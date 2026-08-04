package com.cjeme26.farclimb.client.render;

import com.cjeme26.farclimb.FarClimb;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Temporary low-poly wall model for an attached climbing axe.
 *
 * Unlike Minecraft's normal item model, this geometry is authored around the
 * exact wall bite: local (0, 0, 0) is the tip of the pick. The renderer can
 * therefore rotate the handle around a stable contact without guessing at the
 * hidden pivot used by ModelTransformationMode.FIXED.
 */
public final class TemporaryAnchoredAxeModel {
    private static final Identifier HANDLE_TEXTURE = FarClimb.id(
            "textures/entity/anchored_axe_handle.png"
    );
    private static final Identifier METAL_TEXTURE = FarClimb.id(
            "textures/entity/anchored_axe_metal.png"
    );

    private static final ModelPart HANDLE = createHandleModel();
    private static final ModelPart METAL = createMetalModel();

    private TemporaryAnchoredAxeModel() {
    }

    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light
    ) {
        VertexConsumer handleVertices = consumers.getBuffer(
                RenderLayer.getEntityCutout(HANDLE_TEXTURE)
        );
        HANDLE.render(
                matrices,
                handleVertices,
                light,
                OverlayTexture.DEFAULT_UV
        );

        VertexConsumer metalVertices = consumers.getBuffer(
                RenderLayer.getEntityCutout(METAL_TEXTURE)
        );
        METAL.render(
                matrices,
                metalVertices,
                light,
                OverlayTexture.DEFAULT_UV
        );
    }

    private static ModelPart createHandleModel() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        // The handle starts just behind the head and extends downward. All
        // dimensions are model pixels; ModelPart renders them at 1/16 block.
        root.addChild(
                "handle",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -0.75F,
                                2.4F,
                                2.35F,
                                1.5F,
                                11.5F,
                                1.5F,
                                Dilation.NONE
                        ),
                ModelTransform.NONE
        );

        return TexturedModelData.of(data, 16, 16).createModel();
    }

    private static ModelPart createMetalModel() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        // A short spike crosses the wall plane. Its innermost end is the exact
        // local origin, so the visible model cannot drift away from its anchor.
        root.addChild(
                "pick",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -0.65F,
                                -0.65F,
                                -0.70F,
                                1.3F,
                                1.3F,
                                4.2F,
                                Dilation.NONE
                        ),
                ModelTransform.NONE
        );

        // The horizontal head sits outside the rock and joins the handle.
        root.addChild(
                "head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -4.6F,
                                -1.0F,
                                2.55F,
                                9.2F,
                                2.0F,
                                2.0F,
                                Dilation.NONE
                        ),
                ModelTransform.NONE
        );

        // A small rear block gives the temporary silhouette more depth without
        // committing to the final artwork.
        root.addChild(
                "rear_weight",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                -1.25F,
                                0.35F,
                                2.45F,
                                2.5F,
                                2.0F,
                                2.2F,
                                Dilation.NONE
                        ),
                ModelTransform.NONE
        );

        return TexturedModelData.of(data, 16, 16).createModel();
    }
}
