package com.example.vendeFacil.util;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Utilitario para padronizar a logo da loja num quadrado de 400x400 pixels.
// Faz um recorte central (cover): a imagem PREENCHE todo o quadrado, mantendo
// a proporcao (as sobras das bordas mais longas sao cortadas). Como as telas
// exibem a logo em uma moldura redonda, o resultado fica preenchido no circulo.
public final class ImagemUtil {

    private static final int TAMANHO = 400;

    private ImagemUtil() {
    }

    public static byte[] paraQuadrado400(byte[] original) {
        if (original == null || original.length == 0) {
            throw new IllegalArgumentException("Imagem vazia");
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
            if (img == null) {
                throw new IllegalArgumentException("Arquivo de imagem inválido");
            }

            // Maior quadrado central possivel da imagem original.
            int lado = Math.min(img.getWidth(), img.getHeight());
            int sx = (img.getWidth() - lado) / 2;
            int sy = (img.getHeight() - lado) / 2;

            BufferedImage canvas = new BufferedImage(TAMANHO, TAMANHO, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Recorte central da origem escalado para 400x400 (preenche tudo).
            g.drawImage(img, 0, 0, TAMANHO, TAMANHO, sx, sy, sx + lado, sy + lado, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível processar a imagem");
        }
    }
}
