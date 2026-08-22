package hu.zoldleo.embers.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.datacomponents.GemSocketComponent;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DawnstoneShieldModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Embers.res("dawnstone_shield"), "main");
    public static final Material MATERIAL = new Material(InventoryMenu.BLOCK_ATLAS, Embers.res("item/dawnstone_shield"));

    private final ModelPart root;
    private final ModelPart plate;
    private final ModelPart handle;
    private final ModelPart[] gems;

    public DawnstoneShieldModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.root = root;
        this.plate = root.getChild("plate");
        this.handle = root.getChild("handle");
        this.gems = new ModelPart[3];

        ModelPart gem_root = plate.getChild("gems");
        for (int i = 0; i < gems.length; i++)
            gems[i] = gem_root.getChild("gem_" + i);
    }

    public void setup(ItemStack itemStack) {
        if (itemStack.has(RegistryManager.GEM_SOCKET_COMPONENT)) {
            GemSocketComponent component = itemStack.get(RegistryManager.GEM_SOCKET_COMPONENT);
            if (component != null) {
                ItemStack[] socketedGems = component.getAttachedGems();
                for (int i = 0; i < gems.length && i < socketedGems.length; i++)
                    gems[i].visible = !socketedGems[i].isEmpty();
            }
            return;
        }
        for (ModelPart gem : gems)
            gem.visible = false;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        PartDefinition plate = root.addOrReplaceChild("plate", CubeListBuilder.create(), PartPose.ZERO);
        plate.addOrReplaceChild("center_plate", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4, -12, -2, 8, 24, 4),
                PartPose.offsetAndRotation(0, 0, -3, 0.0436f, 0, 0)); // 2.5 deg = 0.0436 rad
        plate.addOrReplaceChild("left_plate", CubeListBuilder.create()
                        .texOffs(24, 28)
                        .addBox(-6, -14, -1, 10, 20, 2),
                PartPose.offsetAndRotation(4, 4, -2.4f, 0, -0.1745f, 0.3927f)); // -10 deg = -0.1745 rad, 22.5 deg = 0.3927 rad
        plate.addOrReplaceChild("right_plate", CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-4, -14, -1, 10, 20, 2),
                PartPose.offsetAndRotation(-4, 4, -2.4f, 0, 0.1745f, -0.3927f)); // 10 deg = 0.1745 rad, -22.5 deg = -0.3927 rad

        PartDefinition gems = plate.addOrReplaceChild("gems", CubeListBuilder.create(), PartPose.ZERO);
        gems.addOrReplaceChild("gem_0", CubeListBuilder.create()
                        .texOffs(24, 12)
                        .addBox(-1, -7, -3, 2, 2, 2),
                PartPose.offsetAndRotation(0, 0, -3, 0.0436f, 0, 0)); // 2.5 deg = 0.0436 rad
        gems.addOrReplaceChild("gem_1", CubeListBuilder.create()
                        .texOffs(24, 12)
                        .addBox(-1, -1, -3, 2, 2, 2),
                PartPose.offsetAndRotation(0, 0, -3, 0.0436f, 0, 0)); // 2.5 deg = 0.0436 rad
        gems.addOrReplaceChild("gem_2", CubeListBuilder.create()
                        .texOffs(24, 12)
                        .addBox(-1, 5, -3, 2, 2, 2),
                PartPose.offsetAndRotation(0, 0, -3, 0.0436f, 0, 0)); // 2.5 deg = 0.0436 rad

        root.addOrReplaceChild("handle", CubeListBuilder.create()
                .texOffs(24, 0)
                .addBox(-1, -3, -1, 2, 6, 6),
                PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public ModelPart plate() {
        return plate;
    }

    public ModelPart handle() {
        return handle;
    }

    public ModelPart[] gems() {
        return gems;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}