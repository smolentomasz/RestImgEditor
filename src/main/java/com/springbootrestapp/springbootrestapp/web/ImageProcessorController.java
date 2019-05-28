package com.springbootrestapp.springbootrestapp.web;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.ServletInputStream;

public class ImageProcessorController {
    private Map<String, BufferedImage> binaryMap = new HashMap<>();

    public BufferedImage cloneImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);

        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    public byte[] bufferedToByte(BufferedImage originalImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( originalImage, "png", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }
    public JSONObject setImage(ServletInputStream inputStream) throws IOException {
        BufferedImage uploadedImage = ImageIO.read(inputStream);
        String uniqueID = UUID.randomUUID().toString();
        binaryMap.put(uniqueID, uploadedImage);
        System.out.println("ROzmiar mapy: " + binaryMap.size());
        JSONObject newMapObject = new JSONObject();
        newMapObject.put("Id", uniqueID);
        newMapObject.put("Width", uploadedImage.getWidth());
        newMapObject.put("Height", uploadedImage.getHeight());
        return newMapObject;
    }
    public void deleteImageFromMap(String imageID){
        if(binaryMap.containsKey(imageID)){
            binaryMap.remove(imageID);
            System.out.println("ROzmiar mapy po usunieciu: " + binaryMap.size());
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }

    }
    public JSONObject getImageSize(String imageId){
        if(binaryMap.containsKey(imageId)){
            int width = binaryMap.get(imageId).getWidth();
            int height = binaryMap.get(imageId).getHeight();

            JSONObject newImageSize = new JSONObject();
            newImageSize.put("Width",width);
            newImageSize.put("Height",height);

            return newImageSize;
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
    public byte[] scaleImage(String imageId, String percentage) throws IOException {
        if(binaryMap.containsKey(imageId)){
            BufferedImage newImg = cloneImage(binaryMap.get(imageId));
            double height = binaryMap.get(imageId).getHeight();
            double width = binaryMap.get(imageId).getWidth();

            double percentageScale = Double.parseDouble(percentage);

            int scaledWidth = (int)(percentageScale/100 * width);
            int scaledHeigh = (int)(percentageScale/100 * height);

            BufferedImage bicubicImage = new BufferedImage(scaledWidth, scaledHeigh, BufferedImage.TYPE_INT_RGB);

            Graphics2D bg = bicubicImage.createGraphics();

            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            bg.scale(percentageScale/100, percentageScale/100);

            bg.drawImage(newImg, 0, 0, null);
            bg.dispose();

            byte[] imgInByte = bufferedToByte(bicubicImage);

            return imgInByte;
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }

    }
    public byte[] cropImage(String imageId, int start, int stop, int width, int height) throws IOException{
        if(binaryMap.containsKey(imageId)){
            if((start >= 0 && start <= binaryMap.get(imageId).getWidth()) && stop >= 0 && stop <= binaryMap.get(imageId).getHeight() && (binaryMap.get(imageId).getWidth() - start) >= width && (binaryMap.get(imageId).getHeight() - stop >= height) ){
                BufferedImage croppedImage = binaryMap.get(imageId).getSubimage(start, stop, width, height);

                byte[] imgInByte = bufferedToByte(croppedImage);

                return imgInByte;
            }
            else{
                throw new ResponseStatusException(
                        HttpStatus.NOT_ACCEPTABLE, "One of the arguments is not correct!"
                );
            }
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
    public JSONObject imageHistogram(String imageId) {
        if (binaryMap.containsKey(imageId)) {
            BufferedImage image = binaryMap.get(imageId);
            double R[] = new double[256];
            double G[] = new double[256];
            double B[] = new double[256];
            int rgb, r, g, b;

            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    rgb = image.getRGB(i, j);
                    r = (rgb >> 16) & 0xff;
                    g = (rgb >> 8) & 0xff;
                    b = (rgb) & 0xff;
                    R[r]++;
                    G[g]++;
                    B[b]++;
                }
            }

            double maxR = R[0];
            double maxG = G[0];
            double maxB = B[0];

            for(int i=1;i<255;i++){
                if(maxR < R[i])
                    maxR = R[i];
                if(maxG < G[i])
                    maxG = G[i];
                if(maxB < B[i])
                    maxB = B[i];
            }

            JSONObject jsonObject = new JSONObject();
            JSONObject rObject = new JSONObject();
            JSONObject gObject = new JSONObject();
            JSONObject bObject = new JSONObject();

            for (int i = 0; i < 256; i++) {
                rObject.put(i, R[i]/maxR);
                gObject.put(i, G[i]/maxG);
                bObject.put(i, B[i]/maxB);
            }
            jsonObject.put("R", rObject);
            jsonObject.put("G", gObject);
            jsonObject.put("B", bObject);

            return jsonObject;
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
    public byte[] greyImage(String imageId) throws Exception {
        if(binaryMap.containsKey(imageId)) {
            BufferedImage newImg = cloneImage(binaryMap.get(imageId));

            for(int i =0;i<newImg.getWidth();i++) {
                for(int j =0;j<newImg.getHeight();j++) {
                    Color c = new Color(newImg.getRGB(i, j));
                    int R = (int) (c.getRed() * 0.29);
                    int G = (int) (c.getGreen() * 0.587);
                    int B = (int) (c.getBlue() * 0.114);
                    Color grey = new Color(R + G + B, R + G + B, R + G + B);
                    newImg.setRGB(i, j, grey.getRGB());
                }
            }
            BufferedImage grayImage = new BufferedImage(newImg.getWidth(), newImg.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = grayImage.createGraphics();

            bg.drawImage(newImg, 0, 0, null);
            bg.dispose();

            byte[] imgInByte = bufferedToByte(grayImage);

            return imgInByte;
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
    public byte[] bluredImage(String imageId, float radius) throws IOException {
        if(binaryMap.containsKey(imageId)) {
            BufferedImage image = binaryMap.get(imageId);
            BufferedImage clone = cloneImage(image);

            GaussianBlur gaussianFilter = new GaussianBlur(radius, 100);
            clone = gaussianFilter.filter(image, clone, true);

            return bufferedToByte(clone);
        }
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
}
