/**
 * 
 */
package test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import JOGL.DDSImage;
import Model.DDSFile;
import Model.DDSImageFile;

/**
 * @author danielsenff
 *
 */
public class DDSTestCase extends TestCase {

	String outputDirectory = "/Users/danielsenff/Programming/Testing/";
	String inputDirectory = "test/resource/";
	
	File texturePNG2048 = new File(inputDirectory +"STRBahrain.png");
	File textureDDS1024 = new File(inputDirectory +"RAIKKONENEXTRA0.dds");
	File textureBMP512x512 = new File(inputDirectory +"512x512.bmp");
	File texturePNG64x64 = new File(inputDirectory + "SUAEXTRA3-1.png");
	File imageBMP3 = new File(inputDirectory +"3x3.bmp");
	File originalB1024 = new File(inputDirectory +"RAIKKONENEXTRA1.dds");
	File original2048 = new File(inputDirectory +"RAIKKONENEXTRA0.dds");
	
	
	public static DDSImageFile loadDDSImageFile(File file) {
		DDSImageFile ddsimage = null;
		ddsimage = new DDSImageFile(file);
		return ddsimage;
	}
	
	public static DDSFile loadDDSFile(File file) {
		DDSFile ddsimage = null;
		ddsimage = new DDSImageFile(file);
		return ddsimage;
	}
	
	public static DDSImage loadDDSImage(File file) {
		DDSImage ddsimage = null;
		try {
			ddsimage = DDSImage.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ddsimage;
	}
	
	public static void assertDDSImage(final DDSImageFile ddsimage, final DDSImageFile newddsimage) {
		assertEquals("height",ddsimage.getHeight(), newddsimage.getHeight());
		assertEquals("width", ddsimage.getWidth(), newddsimage.getWidth());
		assertEquals("is compressed", ddsimage.isCompressed(), newddsimage.isCompressed());
		assertEquals("pixelformat", ddsimage.getPixelformat(), newddsimage.getPixelformat());
		assertEquals("has MipMaps", ddsimage.hasMipMaps(), newddsimage.hasMipMaps());
		if (ddsimage.hasMipMaps() && newddsimage.hasMipMaps()) {
			assertEquals("number of MipMaps", ddsimage.getNumMipMaps(), newddsimage.getNumMipMaps());
		}
	}
	
	public void testSelf() {
		assertEquals("just something to get the green bar :)", true, true);
	}
}