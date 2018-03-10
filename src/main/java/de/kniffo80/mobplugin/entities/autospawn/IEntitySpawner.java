package de.kniffo80.mobplugin.entities.autospawn;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import java.util.List;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public interface IEntitySpawner {

    public void spawn(List<Player> onlinePlayers);

    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level);

    public int getEntityNetworkId();

    public String getEntityName();

}
