package com.springbootrestapp.springbootrestapp.web;

import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SpringBootRestappController {
    private ImageProcessorController imageProcessorController = new ImageProcessorController();
    @RequestMapping(value = "/image/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject addImage(HttpServletRequest requestEntity) throws Exception {
        return imageProcessorController.setImage(requestEntity.getInputStream());
    }
    @RequestMapping(value = "/image/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteImage(@PathVariable String id){
        imageProcessorController.deleteImageFromMap(id);
        return null;
    }
    @RequestMapping(value = "/image/{id}/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getImageSize(@PathVariable String id){
        return imageProcessorController.getImageSize(id);
    }
    @RequestMapping(value = "/image/{id}/scale/{percent}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getScaledImage(@PathVariable String id, @PathVariable String percent) throws Exception {
        return imageProcessorController.scaleImage(id, percent);
    }
    @RequestMapping(value = "/image/{id}/crop/{start}/{stop}/{width}/{height}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getCropImage(@PathVariable String id, @PathVariable int start, @PathVariable int stop, @PathVariable int width, @PathVariable int height) throws Exception {
        return imageProcessorController.cropImage(id, start, stop, width, height);
    }
    @RequestMapping(value = "/image/{id}/histogram", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject getHistogram(@PathVariable String id){
        return imageProcessorController.imageHistogram(id);
    }
    @RequestMapping(value = "/image/{id}/greyscale", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getGrayImage(@PathVariable String id) throws Exception {
        return imageProcessorController.greyImage(id);
    }
    @RequestMapping(value = "/image/{id}/blur/{radius}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getBluredImage(@PathVariable String id, @PathVariable float radius) throws IOException {
        return imageProcessorController.bluredImage(id, radius);
    }
}
