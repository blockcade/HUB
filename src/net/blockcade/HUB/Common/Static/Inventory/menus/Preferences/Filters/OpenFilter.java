package net.blockcade.HUB.Common.Static.Inventory.menus.Preferences.Filters;

import net.blockcade.HUB.Common.GamePlayer;
import net.blockcade.HUB.Common.Static.Inventory.Manager;
import net.blockcade.HUB.Common.Static.Preferances.FilterVisibility;
import net.blockcade.HUB.Common.Utils.Item;
import net.blockcade.HUB.Common.Utils.Text;
import net.blockcade.HUB.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class OpenFilter {

    public static Item getMenuItem(GamePlayer gameplayer) {
        ItemStack is = new ItemStack(Material.LIME_DYE);

        if(gameplayer.getPreferenceSettings().getFilterVisibility().equals(FilterVisibility.OPEN)){
            item.setEnchanted(true);
        }

        Item item = new Item(is,"&2OPEN &bfilter");
        item.setLore("","&7no filter will be applied","");
        item.setOnClick(new Item.click(){

            @Override
            public void run(Player param1Player) {

                

            }
        });
        return item;
    }

}
