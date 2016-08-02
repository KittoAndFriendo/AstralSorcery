package hellfirepvp.astralsorcery.common.data.world.data;

import hellfirepvp.astralsorcery.common.data.world.CachedWorldData;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RockCrystalBuffer
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:30
 */
public class RockCrystalBuffer extends CachedWorldData {

    private Map<ChunkPos, List<BlockPos>> crystalPositions = new HashMap<>();
    private static final Object lock = new Object();

    public RockCrystalBuffer() {
        super(WorldCacheManager.SaveKey.ROCK_CRYSTAL);
    }

    @Override
    public RockCrystalBuffer constructNewData() {
        return new RockCrystalBuffer();
    }

    @Override
    public void updateTick() {}

    public void addOre(BlockPos pos) {
        ChunkPos ch = new ChunkPos(pos);
        synchronized (lock) {
            if(!crystalPositions.containsKey(ch)) {
                crystalPositions.put(ch, new LinkedList<>());
            }
            crystalPositions.get(ch).add(pos);
        }

        markDirty();
    }

    public void removeOre(BlockPos pos) {
        ChunkPos ch = new ChunkPos(pos);
        if(!crystalPositions.containsKey(ch)) return;
        boolean removed;
        synchronized (lock) {
            removed = crystalPositions.get(ch).remove(pos);
            if(crystalPositions.get(ch).size() == 0) {
                crystalPositions.remove(ch);
                removed = true;
            }
        }

        if(removed) {
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        synchronized (lock) {
            crystalPositions.clear();
        }

        Map<ChunkPos, List<BlockPos>> work = new HashMap<>();
        if(nbt.hasKey("crystalList")) {
            NBTTagList list = nbt.getTagList("crystalList", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound chList = list.getCompoundTagAt(i);
                int chX = chList.getInteger("chX");
                int chZ = chList.getInteger("chZ");
                ChunkPos pos = new ChunkPos(chX, chZ);
                List<BlockPos> positions = new LinkedList<>();
                NBTTagList entries = chList.getTagList("crystals", 10);
                for (int j = 0; j < entries.tagCount(); j++) {
                    NBTTagCompound tag = entries.getCompoundTagAt(i);
                    int x = tag.getInteger("x");
                    int y = tag.getInteger("y");
                    int z = tag.getInteger("z");
                    positions.add(new BlockPos(x, y, z));
                }
                work.put(pos, positions);
            }
        }

        synchronized (lock) {
            crystalPositions = work;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList listCrystals = new NBTTagList();
        synchronized (lock) {
            for (ChunkPos pos : crystalPositions.keySet()) {
                NBTTagCompound comp = new NBTTagCompound();
                comp.setInteger("chX", pos.chunkXPos);
                comp.setInteger("chZ", pos.chunkZPos);
                NBTTagList chList = new NBTTagList();
                for (BlockPos exactPos : crystalPositions.get(pos)) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setInteger("x", exactPos.getX());
                    tag.setInteger("y", exactPos.getY());
                    tag.setInteger("z", exactPos.getZ());
                    chList.appendTag(tag);
                }
                comp.setTag("crystals", chList);
                listCrystals.appendTag(comp);
            }
        }
        nbt.setTag("crystalList", listCrystals);
        return nbt;
    }

}
