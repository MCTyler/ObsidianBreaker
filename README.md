# ObsidianBreaker
Updated to 1.9.4 NMS
Obsidian Breaker

Damaged obsidian
What is this?

This is a plugin that allows players to destroy obsidian, ender chests, enchanting tables or custom blocks with TNT and creepers among other things after a set amount of explosions. Really useful for faction servers.
It will also display cracks on the block depending on the remaining health of the block (optional).

How to use it?

You're ready to roll, just drop this in your plugin folder. You may however consider changing the default configuration.

How to configure it then?
Permissions
obsidianbreaker.test	Allow user to test the durability of a block using the specified tool
obsidianbreaker.reload	Allow user to reload the config using /ob reload
	
Configuration
BlastRadius	How large the blast radius should be. (Only applies to this plugin)
LiquidMultiplier	How many times harder it should be to damage the block if there's water there.
DurabilityChecker	Specify which item should be used to check the damage on the block. Stick (280) is default.
DropChance	How big chance (in percent) is there that a broken block will give drops. Default in Minecraft was 30 %.
Blocks	Specify which items this plugin applies to and the required amount of hits
(item id): (required hits)
Regen	Frequency: How often blocks should regenerate in minutes. Set to -1 if you want to disable.
Amount: How many hits the block should regenerate
BlockCracks	Enable: Set whether block cracks are enabled or not.
Interval: How often the server should refresh the client (no more than 15 seconds is recommended!)
ExplosionSources	Specify how much damage a specific explosion source should make to blocks handled by this plugin
(entity name): (damage)	


