/*
 *
 *  *
 *  * © Stelch Software 2019, distribution is strictly prohibited
 *  * Blockcade is a company of Stelch Software
 *  *
 *  * Changes to this file must be documented on push.
 *  * Unauthorised changes to this file are prohibited.
 *  *
 *  * @author Ryan Wood
 *  @since 5/8/2019
 */

package net.blockcade.HUB.Common;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.blockcade.HUB.Common.Static.GameSearch;
import net.blockcade.HUB.Common.Static.GameServer;
import net.blockcade.HUB.Common.Static.Variables.Game;
import net.blockcade.HUB.Common.Static.Variables.Ranks;
import net.blockcade.HUB.Common.Utils.SQL;
import net.blockcade.HUB.Common.Utils.Text;
import net.blockcade.HUB.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class GamePlayer {

    private Player player;
    private UUID uuid;

    private PreferenceSettings preferenceSettings;
    private boolean isBuilt=false;
    private String name;
    private int level=0;
    private Ranks rank=Ranks.MEMBER;
    private GameParty party=null;

    private ArrayList<GamePlayer> friends = new ArrayList<>();
    private long friends_last_fetch = 0;

    /**
     *
     * @param player Bukkit Player to be resolved from SQL Database.
     */
    public GamePlayer(Player player){this.player=player;this.name=player.getName();this.uuid=player.getUniqueId();}
    public GamePlayer(String player){this.name=player;}
    public GamePlayer(UUID uuid){this.uuid=uuid;}

    /**
     * This function will send the GamePlayer a formatted message, &( [0-9] [a-f] [i-o] )
     * sent by Text Chat
     * @param message non formatted message to be sent to player
     * @since 5/08/2019
     */
    public void sendMessage(String message) { Text.sendMessage(player,message, Text.MessageType.TEXT_CHAT); }

    /**
     * This function will send the GamePlayer a formatted message, &( [0-9] [a-f] [i-o] )
     * sent by Title Screen
     * @param message non formatted message to be sent to player
     * @since 5/08/2019
     */
    public void sendTitle(String message) { Text.sendMessage(player,message, Text.MessageType.TITLE); }

    /**
     * This function will send the GamePlayer a formatted message, &( [0-9] [a-f] [i-o] )
     * sent by Action Bar
     * @param message non formatted message to be sent to player
     * @since 5/08/2019
     */
    public void sendActionBar(String message) { Text.sendMessage(player,message, Text.MessageType.ACTION_BAR); }

    /**
     *
     * @param server BungeeCord server in which to send the player to
     */
    public void sendServer(GameServer server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server.getName());
        player.sendPluginMessage(Main.getPlugin(Main.class), "BungeeCord", out.toByteArray());
    }

    public void joinQueue(Game game){
        if(Main.Searching.containsKey(this)&&Main.Searching.get(this).getGame().equals(game)){return;}
        GameSearch search = new GameSearch(this,game);
        if(Main.Searching.containsKey(player))Main.Searching.remove(this);
        Main.Searching.put(this,search);
    }

    /**
     *
     * @return GamePlayer UUID (Online Version)
     * @since 5/08/2019
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     *
     * @return Get GamePlayer rank
     * @since 5/08/2019
     */
    public Ranks getRank() {
        if(!isBuilt)throw new Error("Failed getting rank, the specified GamePlayer has not been built.");
        return rank;
    }

    /**
     *
     * @return Player network wide level
     */
    public int getLevel() {
        return level;
    }

    /**
     *
     * @return Player display name (Nickname)
     */
    public String getName() {
        return name;
    }
    /**
     * TODO BungeeCord implementation for parties
     * @return Player party, fetched by BungeeCord if updated.
     * @since 5/08/2019
     */
    public GameParty getParty() {
        if(!isBuilt)throw new Error("Failed getting party, the specified GamePlayer has not been built.");
        return party;
    }

    /**
     *
     * TODO Add database integration
     * @param player Target player to check relationship to
     * @return Weather the two players are friends
     */
    public boolean isFriend(GamePlayer player){
        return getFriends().contains(player);
    }

    public ArrayList<GamePlayer> getFriends(){
        ArrayList<GamePlayer> friends = new ArrayList<>();

        if(((friends_last_fetch-Calendar.getInstance().getTimeInMillis())<60000&&friends_last_fetch!=0)){
            return this.friends;
        }
        friends_last_fetch=Calendar.getInstance().getTimeInMillis();
        FileConfiguration config = Main.getPlugin(Main.class).getConfig();
        SQL sql = new SQL(config.getString("sql.host"),config.getInt("sql.port"),config.getString("sql.user"),config.getString("sql.pass"),config.getString("sql.database"));
        ResultSet results = sql.query(String.format("SELECT * FROM `relationships` WHERE `player`='%s' LIMIT 25;",this.getUuid()));
        try {
            while(results.next()){
                GamePlayer player = new GamePlayer(UUID.fromString(results.getString("target")));
                player.BuildPlayer();
                friends.add(player);
            }
        }catch (SQLException e){
            System.out.println("| ---------------------------- |");
            System.out.println("|  This error was posted by GamePlayer.java getFriends");
            e.printStackTrace();
            System.out.println("| ---------------------------- |");
        }
        this.friends=friends;
        sql.close();
        return friends;
    }

    /**
     * Has the players data been fetched
     * @return TRUE/FALSE if player built
     */
    public boolean isBuilt() {
        return isBuilt;
    }

    /**
     *
     * @param rank set a player rank, use with caution!
     * @since 5/08/2019
     */
    public void setRank(Ranks rank) {
        if(!isBuilt)throw new Error("Failed setting rank, the specified GamePlayer has not been built.");
        this.rank = rank;
        Main.getSqlConnection().query(String.format("UPDATE `players` SET `rank`='%s' WHERE `uuid`='%s'",rank.name().toUpperCase(),uuid),true);
    }

    /**
     *
     * @param party Override existing GameParty, use of this function can cause issues!
     * @since 5/08/2019
     */
    public void setParty(GameParty party) {
        if(!isBuilt)throw new Error("Failed setting party, the specified GamePlayer has not been built.");
        this.party = party;
    }

    public PreferenceSettings getPreferenceSettings() {
        if(!isBuilt)throw new Error("Failed getting preferences, the specified GamePlayer has not been built.");
        return preferenceSettings;
    }

    public void BuildPlayer() {
        FileConfiguration config = Main.getPlugin(Main.class).getConfig();
        SQL sql = new SQL(config.getString("sql.host"),config.getInt("sql.port"),config.getString("sql.user"),config.getString("sql.pass"),config.getString("sql.database"));
        String query = "";
        if(this.name!=null)query=String.format("SELECT * FROM `players` WHERE `username`='%s' LIMIT 1;",this.name);
        if(query.equals("")&&this.uuid!=null)query=String.format("SELECT * FROM `players` WHERE `uuid`='%s' LIMIT 1;",this.uuid);
        ResultSet results = sql.query(query);
        try {
            while(results.next()){
                this.name=results.getString("username");
                this.rank=(Ranks.valueOf(results.getString("rank").toUpperCase()));
                this.level=results.getInt("level");
                this.uuid=(UUID.fromString(results.getString("uuid")));
                if(this.player!=null&&this.player.isOnline())this.preferenceSettings=new PreferenceSettings(this);
            }
            this.isBuilt=true;
            return;
        }catch (SQLException e){
            System.out.println("| ---------------------------- |");
            System.out.println("|  This error was posted by GamePlayer.java BuildPlayer");
            e.printStackTrace();
            System.out.println("| ---------------------------- |");
        }
        sql.close();
        if(!isBuilt){
            player.kickPlayer(Text.format("&cAn error has occurred in the connection, try again later."));
        }
    }

    public Player spigot() {return this.player;}

    public static GamePlayer getGamePlayer(Player player){
        if(Main.GamePlayers.containsKey(player))
            return Main.GamePlayers.get(player);
        return new GamePlayer(player);
    }
}
