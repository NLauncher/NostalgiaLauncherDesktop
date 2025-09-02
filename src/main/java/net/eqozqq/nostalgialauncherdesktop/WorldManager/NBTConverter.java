package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import java.util.ArrayList;
import java.util.List;
import org.spout.nbt.*;

public final class NBTConverter {

    public static Level readLevel(CompoundTag compoundTag) {
        Level level = new Level();
        List<Tag> tags = compoundTag.getValue();
        for (Tag tag : tags) {
            String name = tag.getName();
            if (name.equals("GameType")) {
                level.setGameType(((IntTag) tag).getValue());
            } else if (name.equals("LastPlayed")) {
                level.setLastPlayed(((LongTag) tag).getValue());
            } else if (name.equals("LevelName")) {
                level.setLevelName(((StringTag) tag).getValue());
            } else if (name.equals("Player")) {
                level.setPlayer(readPlayer((CompoundTag) tag));
            } else if (name.equals("RandomSeed")) {
                level.setRandomSeed(((LongTag) tag).getValue());
            } else if (name.equals("SpawnX")) {
                level.setSpawnX(((IntTag) tag).getValue());
            } else if (name.equals("SpawnY")) {
                level.setSpawnY(((IntTag) tag).getValue());
            } else if (name.equals("SpawnZ")) {
                level.setSpawnZ(((IntTag) tag).getValue());
            } else if (name.equals("Time")) {
                level.setTime(((LongTag) tag).getValue());
            } else if (name.equals("dayCycleStopTime")) {
                level.setDayCycleStopTime(((LongTag) tag).getValue());
            } else if (name.equals("spawnMobs")) {
                level.setSpawnMobs(((ByteTag) tag).getValue() != 0);
            }
        }
        return level;
    }

    @SuppressWarnings("unchecked")
    public static Player readPlayer(CompoundTag compoundTag) {
        Player player = new Player();
        List<Tag> tags = compoundTag.getValue();
        for (Tag tag : tags) {
            String name = tag.getName();
            if (name.equals("Pos")) {
                player.setLocation(readVector((ListTag<FloatTag>) tag));
            } else if (name.equals("abilities")) {
                readAbilities((CompoundTag) tag, player.getAbilities());
            }
        }
        return player;
    }

    public static void readAbilities(CompoundTag tag, PlayerAbilities abilities) {
        List<Tag> tags = tag.getValue();
        for (Tag t : tags) {
            String n = t.getName();
            if (!(t instanceof ByteTag)) continue;
            boolean value = ((ByteTag) t).getValue() != 0;
            if (n.equals("flying")) {
                abilities.flying = value;
            } else if (n.equals("instabuild")) {
                abilities.instabuild = value;
            } else if (n.equals("invulnerable")) {
                abilities.invulnerable = value;
            } else if (n.equals("mayfly")) {
                abilities.mayFly = value;
            }
        }
    }

    public static Vector3f readVector(ListTag<FloatTag> tag) {
        List<FloatTag> tags = tag.getValue();
        return new Vector3f(tags.get(0).getValue(), tags.get(1).getValue(), tags.get(2).getValue());
    }

    public static CompoundTag writeLevel(Level level) {
        List<Tag> tags = new ArrayList<>();
        tags.add(new IntTag("GameType", level.getGameType()));
        tags.add(new LongTag("LastPlayed", level.getLastPlayed()));
        tags.add(new StringTag("LevelName", level.getLevelName()));
        tags.add(writePlayer(level.getPlayer(), "Player"));
        tags.add(new LongTag("RandomSeed", level.getRandomSeed()));
        tags.add(new IntTag("SpawnX", level.getSpawnX()));
        tags.add(new IntTag("SpawnY", level.getSpawnY()));
        tags.add(new IntTag("SpawnZ", level.getSpawnZ()));
        tags.add(new LongTag("Time", level.getTime()));
        tags.add(new LongTag("dayCycleStopTime", level.getDayCycleStopTime()));
        tags.add(new ByteTag("spawnMobs", level.getSpawnMobs()));
        return new CompoundTag("", tags);
    }

    public static CompoundTag writePlayer(Player player, String name) {
        List<Tag> tags = new ArrayList<>();
        if (player.getLocation() != null) {
            tags.add(writeVector(player.getLocation(), "Pos"));
        }
        tags.add(writeAbilities(player.getAbilities(), "abilities"));
        return new CompoundTag(name, tags);
    }

    public static CompoundTag writeAbilities(PlayerAbilities abilities, String name) {
        List<Tag> values = new ArrayList<>(4);
        values.add(new ByteTag("flying", abilities.flying));
        values.add(new ByteTag("instabuild", abilities.instabuild));
        values.add(new ByteTag("invulnerable", abilities.invulnerable));
        values.add(new ByteTag("mayfly", abilities.mayFly));
        return new CompoundTag(name, values);
    }
    
    public static ListTag<FloatTag> writeVector(Vector3f vector, String tagName) {
		List<FloatTag> tags = new ArrayList<FloatTag>(3);
		tags.add(new FloatTag("", vector.getX()));
		tags.add(new FloatTag("", vector.getY()));
		tags.add(new FloatTag("", vector.getZ()));
		return new ListTag<FloatTag>(tagName, FloatTag.class, tags);
	}
}