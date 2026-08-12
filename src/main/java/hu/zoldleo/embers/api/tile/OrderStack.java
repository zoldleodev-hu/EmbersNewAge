package hu.zoldleo.embers.api.tile;

import hu.zoldleo.embers.api.filter.IFilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

public class OrderStack {
    private BlockPos pos;
    private IFilter filter;
    private int size;

    public OrderStack(BlockPos pos, IFilter filter, int size) {
        this.pos = pos;
        this.filter = filter;
        this.size = size;
    }

    public OrderStack(CompoundTag tag, HolderLookup.Provider provider) {
        readFromNBT(tag, provider);
    }

    public BlockPos getPos() {
        return pos;
    }

    public IOrderSource getSource(Level world) {
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof IOrderSource)
            return (IOrderSource) tile;
        return null;
    }

    public IFilter getFilter() {
        return filter;
    }

    public int getSize() {
        return size;
    }

    public boolean acceptsItem(Level world, ItemStack stack) {
        IOrderSource source = getSource(world);
        if(source != null) {
            IItemHandler itemHandler = source.getItemHandler();
            if(itemHandler != null)
                return filter.acceptsItem(stack, itemHandler);
        }
        return false;
    }

    public void deplete(int n) {
        size -= n;
    }

    public void increment(int n) {
        size += n;
    }

    public CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("x",pos.getX());
        tag.putInt("y",pos.getY());
        tag.putInt("z",pos.getZ());
        tag.put("filter", filter.writeToNBT(new CompoundTag(), provider));
        tag.putInt("size", size);
        return tag;
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        pos = new BlockPos(tag.getInt("x"),tag.getInt("y"),tag.getInt("z"));
        //TODO: I'll do this later or something
        //filter = FilterUtil.deserializeFilter(tag.getCompound("filter"));
        size = tag.getInt("size");
    }

    public void reset(IFilter filter, int size) {
        this.filter = filter;
        this.size = size;
    }
}