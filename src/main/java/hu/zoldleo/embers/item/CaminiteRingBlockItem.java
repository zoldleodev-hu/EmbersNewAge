package hu.zoldleo.embers.item;

import hu.zoldleo.embers.block.MechEdgeBlockBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CaminiteRingBlockItem extends BlockItem{
	public CaminiteRingBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		InteractionResult interactionresult = null;
		if (context.getClickedFace() == Direction.UP) {
			BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
			if (clickedState.hasProperty(MechEdgeBlockBase.EDGE)) {
				BlockPos pos = context.getClickedPos().offset(clickedState.getValue(MechEdgeBlockBase.EDGE).centerPos);
				interactionresult = this.place(new BlockPlaceContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), context.getHitResult().withPosition(pos)));
			}
		}
		if (interactionresult == null) {
			interactionresult = this.place(new BlockPlaceContext(context));
		}
		if (!interactionresult.consumesAction()) {// && this.isEdible()) { TODO
			InteractionResult interactionresult1 = this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
			return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
		} else {
			return interactionresult;
		}
	}
}