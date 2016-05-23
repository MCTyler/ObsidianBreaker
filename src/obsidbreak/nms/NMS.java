package obsidbreak.nms;

import org.bukkit.Location;

public interface NMS {
	public void sendCrackEffect(Location location, int damage);
	public boolean isDummy();
}
