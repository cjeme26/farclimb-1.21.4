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
 * Temporary diagnostic geometry for the planted axe coordinate system.
 *
 * The small white cube is the exact saved contact. The rods show the local
 * model axes after the wall-facing transform:
 *   red   = +X across the wall face
 *   green = +Y down the wall after the ModelPart Y correction
 *   blue  = +Z outward from the wall
 */
public final class AnchorDebugModel {
    private static final Identifier ORIGIN_TEXTURE = FarClimb.id(
            "textures/entity/anchor_debug_origin.png"
    );
    private static final Identifier X_TEXTURE = FarClimb.id(
            "textures/entity/anchor_debug_x.png"
    );
    private static final Identifier Y_TEXTURE = FarClimb.id(
            "textures/entity/anchor_debug_y.png"
    );
    private static final Identifier Z_TEXTURE = FarClimb.id(
            "textures/entity/anchor_debug_z.png"
    );

    private static final ModelPart ORIGIN = createCuboid(
            -0.8F, -0.8F, -0.8F,
            1.6F, 1.6F, 1.6F
    );
    private static final ModelPart X_AXIS = createCuboid(
            0.0F, -0.35F, -0.35F,
            5.0F, 0.7F, 0.7F
    );
    private static final ModelPart Y_AXIS = createCuboid(
            -0.35F, 0.0F, -0.35F,
            0.7F, 5.0F, 0.7F
    );
    private static final ModelPart Z_AXIS = createCuboid(
            -0.35F, -0.35F, 0.0F,
            0.7F, 0.7F, 5.0F
    );

    private AnchorDebugModel() {
    }

    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light
    ) {
        renderPart(ORIGIN, ORIGIN_TEXTURE, matrices, consumers, light);
        renderPart(X_AXIS, X_TEXTURE, matrices, consumers, light);
        renderPart(Y_AXIS, Y_TEXTURE, matrices, consumers, light);
        renderPart(Z_AXIS, Z_TEXTURE, matrices, consumers, light);
    }

    private static void renderPart(
            ModelPart part,
            Identifier texture,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light
    ) {
        VertexConsumer vertices = consumers.getBuffer(
                RenderLayer.getEntityCutout(texture)
        );
        part.render(
                matrices,
                vertices,
                light,
                OverlayTexture.DEFAULT_UV
        );
    }

    private static ModelPart createCuboid(
            float x,
            float y,
            float z,
            float width,
            float height,
            float depth
    ) {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild(
                "part",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(
                                x,
                                y,
                                z,
                                width,
                                height,
                                depth,
                                Dilation.NONE
                        ),
                ModelTransform.NONE
        );
        return TexturedModelData.of(data, 16, 16).createModel();
    }
}
