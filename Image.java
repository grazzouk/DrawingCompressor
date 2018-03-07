import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

// This class represents a simple rectangular image, where each pixel can be
// one of 16 colours.
public class Image {

    // This is the standard 4-bit EGA colour scheme, where the numbers represent
    // 24-bit RGB colours.
    static int[] colours =
            {0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                    0xAA0000, 0xAA00AA, 0xAA5500, 0xAAAAAA,
                    0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                    0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};
    // Store a 2 dimensional image with "colours" as numbers between 0 and 15
    private int pixels[][];

    // Read in an image from a file. Each line of the file must be the same
    // length, and only contain single digit hex numbers 0-9 and a-f.
    public Image(String filename) {

        // Read the whole file into lines
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            for (String s = in.readLine(); s != null; s = in.readLine())
                lines.add(s);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
            System.exit(1);
        } catch (IOException e) {
            System.exit(2);
        }

        if (lines.size() == 0) {
            System.out.println("Empty file: " + filename);
            System.exit(1);
        }

        // Initialise the array based on the number of lines and the length of the
        // first one.
        int length = lines.get(0).length();
        pixels = new int[lines.size()][length];

        for (int i = 0; i < lines.size(); i++) {
            // Check that all of the lines have the same length as the first one.
            if (length != lines.get(i).length()) {
                System.out.println("Inconsistent line lengths: " + length + " and " + lines.get(i).length() + " on lines 1 and " + (i + 1));
                System.exit(1);
            }

            // Copy each line into the array
            for (int j = 0; j < length; j++) {
                pixels[i][j] = Character.getNumericValue(lines.get(i).charAt(j));
                if (pixels[i][j] < 0 || pixels[i][j] > 15) {
                    System.out.println("Invalid contents: " + lines.get(i).charAt(j) + " on line " + (i + 1));
                    System.exit(1);
                }
            }
        }
    }

    // Create a solid image with given dimensions and colour
    public Image(int height, int width, int colour) {
        pixels = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                pixels[i][j] = colour;
    }

    //a simple method to turn an int value from 0 to 15 to a hex
    public static String toHex(int i) {
        switch (i) {
            case 10:
                return "a";
            case 11:
                return "b";
            case 12:
                return "c";
            case 13:
                return "d";
            case 14:
                return "e";
            case 15:
                return "f";
            default:
                return "" + i;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        /* A test to read in an image, compress it into a drawing file, print it out, and turn it into a png.
        Image i = new Image(args[0]);
        Drawing d = i.compress();
        System.out.print(d.toString());
        System.out.print(d.draw().toString());
        d.draw().toPNG(args[1]);
        i.toPNG(args[1] + " original");*/

        saveFiles();
    }

    /**
     * Creates new drawings for the test files and pixel arts.
     */
    public static void saveFiles() throws FileNotFoundException {
        for (int i = 1; i <= 5; i++) {
            Image ti = new Image("./test-files/test-image" + i);
            //Compress the test image to drawing commands and add it to a file
            try (PrintWriter out = new PrintWriter("./test-files/test-drawing" + i)) {
                out.println(ti.compress().toString());}
            //Create a png image of the compressed image, as well as the original uncompressed one (for comparision)
           /*     ti.compress().draw().toPNG("test-image" + i);
            } catch (BadCommand e) {
                throw new FileNotFoundException();
            }
            ti.toPNG("test-image" + i + "_original");*/

        }
        for (int i = 1; i <= 6; i++) {
            //Compress the pixel art to drawing commands and add it to a file
            Image pa = new Image("./pixel-art/pixel-art" + i);
            //Drawing pad = pa.compress();
            try (PrintWriter out = new PrintWriter("./pixel-art/pixel-drawing" + i)) {
                out.println(pa.compress().toString()); }
           //Create a png image of the compressed image, as well as the original uncompressed one (for comparision)
           /*pad.draw().toPNG("pixel-image" + i);
            } catch (BadCommand e) {
                throw new FileNotFoundException();
            }
            pa.toPNG("pixel-image" + i + "_original");*/
        }

    }

    // Get back the original text-based representation
    public String toString() {
        StringBuilder s = new StringBuilder(pixels.length * pixels[0].length);
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++)
                s.append(Integer.toHexString(pixels[i][j]));
            s.append("\n");
        }
        return s.toString();
    }

    // TASK 2: Implement the compress method to create and return a list of
    // drawing commands that will draw this image.
    // 6 marks for correctness -- does the command list exactly produce the
    // input image.
    // 5 marks for being able to shrink test-image1 and test-image2 into no more
    // than 20 and 35 commands respectively. You can work out these commands by
    // hand, but the marks here are for your implemented algorithm (HINT: think
    // Run-length Encoding) being able to create the commands.
    // 4 marks for shrinking the other, more difficult, test images. We'll run
    // this as a competition and give all 4 to the best 20% of the class, 3 to
    // the next best 20%, and so on.
    public Drawing compress() {
        //initialises the variables used in the code
        int height = pixels.length;
        int width = pixels[0].length;
        int background = pixels[0][0];
        Drawing drawingHor = new Drawing(height, width, background);
        Drawing drawingVer = new Drawing(height, width, background);
        int i = 0;
        int j = 0;
        int runX = 0;
        int runY = 0;
        int[] current = {i, j, pixels[i][j]};
        int[] previous = {i, j, pixels[i][j]};
        boolean lineChanged = false;
        boolean firstChar = true;
        //horizontal RLE compression
        while (i < height) {
            //performs RLE when pointer is at the start of the line
            if (j == 0) {
                current[0] = i;
                current[1] = 0;
                current[2] = pixels[i][0];
                previous[0] = i;
                previous[1] = 0;
                previous[2] = pixels[i][0];
                while (j < width - 1) {
                    current[0] = i;
                    current[1] = j + 1;
                    current[2] = pixels[i][j + 1];
                    if (current[2] == previous[2]) {
                        runX++;
                        j++;
                    } else {
                        if (runX > 0) {
                            if (firstChar) {
                                if (runY > 0) {
                                    DrawingCommand command = new DrawingCommand("down " + runY);
                                    drawingHor.addCommand(command);
                                    runY = 0;
                                }
                                if (pixels[i][0] != background) {
                                    DrawingCommand command = new DrawingCommand("left 1");
                                    drawingHor.addCommand(command);
                                    j--;
                                }
                                firstChar = false;
                            }
                            if (previous[2] == background) {
                                DrawingCommand command = new DrawingCommand("right " + runX);
                                drawingHor.addCommand(command);
                            } else {
                                DrawingCommand command = new DrawingCommand("right " + runX + " " + toHex(previous[2]));
                                drawingHor.addCommand(command);
                            }
                            lineChanged = true;
                            runX = 0;
                        }
                    }
                    previous[0] = current[0];
                    previous[1] = current[1];
                    previous[2] = current[2];
                }

                //performs RLE when pointer is at the end of the line
            } else if (j == width - 1) {
                current[0] = i;
                current[1] = width - 1;
                current[2] = pixels[i][width - 1];
                previous[0] = i;
                previous[1] = width - 1;
                previous[2] = pixels[i][width - 1];
                while (j > 0) {
                    current[0] = i;
                    current[1] = j - 1;
                    current[2] = pixels[i][j - 1];
                    if (current[2] == previous[2]) {
                        runX++;
                        j--;
                    } else if (runX > 0) {
                        if (firstChar) {
                            if (runY > 0) {
                                DrawingCommand command = new DrawingCommand("down " + runY);
                                drawingHor.addCommand(command);
                                runY = 0;
                            }
                            if (pixels[i][width - 1] != background) {
                                DrawingCommand command = new DrawingCommand("right 1");
                                drawingHor.addCommand(command);
                                j++;
                            }
                            firstChar = false;
                        }
                        if (previous[2] == background) {
                            DrawingCommand command = new DrawingCommand("left " + runX);
                            drawingHor.addCommand(command);
                        } else {
                            DrawingCommand command = new DrawingCommand("left " + runX + " " + toHex(previous[2]));
                            drawingHor.addCommand(command);
                        }
                        runX = 0;
                        lineChanged = true;
                    }
                    previous[0] = current[0];
                    previous[1] = current[1];
                    previous[2] = current[2];
                }
            }
            //add the commands that have not been added yet
            if (j == width - 1 && runX > 0) {
                if (current[2] == background) {
                    if (lineChanged) {
                        DrawingCommand command = new DrawingCommand("right " + runX);
                        drawingHor.addCommand(command);
                    } else {
                        j = 0;
                    }
                } else {
                    if (firstChar) {
                        if (runY > 0) {
                            DrawingCommand command = new DrawingCommand("down " + runY);
                            drawingHor.addCommand(command);
                            runY = 0;
                        }
                        if (pixels[i][0] != background) {
                            DrawingCommand command = new DrawingCommand("left 1");
                            drawingHor.addCommand(command);
                            runX++;
                        }
                    }
                    DrawingCommand command = new DrawingCommand("right " + runX + " " + toHex(current[2]));
                    drawingHor.addCommand(command);
                }
                runX = 0;
            } else if (j == 0 && runX > 0) {
                if (current[2] == background) {
                    if (lineChanged) {
                        DrawingCommand command = new DrawingCommand("left " + runX);
                        drawingHor.addCommand(command);
                    } else {
                        j = width - 1;
                    }
                } else {
                    if (firstChar) {
                        if (runY > 0) {
                            DrawingCommand command = new DrawingCommand("down " + runY);
                            drawingHor.addCommand(command);
                            runY = 0;
                        }
                        if (pixels[i][width - 1] != background) {
                            DrawingCommand command = new DrawingCommand("right 1");
                            drawingHor.addCommand(command);
                            runX++;
                        }
                    }
                    DrawingCommand command = new DrawingCommand("left " + runX + " " + toHex(current[2]));
                    drawingHor.addCommand(command);
                }
                runX = 0;
            }
            runY++;
            i++;
            lineChanged = false;
            firstChar = true;
        }

        //Resets all the variables.
        i = 0;
        j = 0;
        runX = 0;
        runY = 0;
        current[0] = i;
        current[1] = j;
        current[2] = pixels[i][j];
        previous[0] = i;
        previous[1] = j;
        previous[2] = pixels[i][j];
        lineChanged = false;
        firstChar = true;
        //Vertical RLE compression
        while (j < width) {
            //performs RLE when pointer is at the start of the line
            if (i == 0) {
                current[0] = 0;
                current[1] = j;
                current[2] = pixels[0][j];
                previous[0] = 0;
                previous[1] = j;
                previous[2] = pixels[0][j];
                while (i < height - 1) {
                    current[0] = i + 1;
                    current[1] = j;
                    current[2] = pixels[i + 1][j];
                    if (current[2] == previous[2]) {
                        runY++;
                        i++;
                    } else {
                        if (runY > 0) {
                            if (firstChar) {
                                if (runX > 0) {
                                    DrawingCommand command = new DrawingCommand("right " + runX);
                                    drawingVer.addCommand(command);
                                    runX = 0;
                                }
                                if (pixels[0][j] != background) {
                                    DrawingCommand command = new DrawingCommand("up 1");
                                    drawingVer.addCommand(command);
                                    i--;
                                }
                                firstChar = false;
                            }
                            if (previous[2] == background) {
                                DrawingCommand command = new DrawingCommand("down " + runY);
                                drawingVer.addCommand(command);
                            } else {
                                DrawingCommand command = new DrawingCommand("down " + runY + " " + toHex(previous[2]));
                                drawingVer.addCommand(command);
                            }
                            lineChanged = true;
                            runY = 0;
                        }
                    }
                    previous[0] = current[0];
                    previous[1] = current[1];
                    previous[2] = current[2];
                }

                //performs RLE when pointer is at the end of the line
            } else if (i == height - 1) {
                current[0] = height - 1;
                current[1] = j;
                current[2] = pixels[height - 1][j];
                previous[0] = height - 1;
                previous[1] = j;
                previous[2] = pixels[height - 1][j];
                while (i > 0) {
                    current[0] = i - 1;
                    current[1] = j;
                    current[2] = pixels[i - 1][j];
                    if (current[2] == previous[2]) {
                        runY++;
                        i--;
                    } else {
                        if (runY > 0) {
                            if (firstChar) {
                                if (runX > 0) {
                                    DrawingCommand command = new DrawingCommand("right " + runX);
                                    drawingVer.addCommand(command);
                                    runX = 0;
                                }
                                if (pixels[height - 1][j] != background) {
                                    DrawingCommand command = new DrawingCommand("down 1");
                                    drawingVer.addCommand(command);
                                    i++;
                                }
                                firstChar = false;
                            }
                            if (previous[2] == background) {
                                DrawingCommand command = new DrawingCommand("up " + runY);
                                drawingVer.addCommand(command);
                            } else {
                                DrawingCommand command = new DrawingCommand("up " + runY + " " + toHex(previous[2]));
                                drawingVer.addCommand(command);
                            }
                            runY = 0;
                            lineChanged = true;
                        }
                    }
                    previous[0] = current[0];
                    previous[1] = current[1];
                    previous[2] = current[2];
                }
            }
            //add the commands that have not been added yet
            if (i == height - 1 && runY > 0) {
                if (current[2] == background) {
                    if (lineChanged) {
                        DrawingCommand command = new DrawingCommand("down " + runY);
                        drawingVer.addCommand(command);
                    } else {
                        i = 0;
                    }
                } else {
                    if (firstChar) {
                        if (runX > 0) {
                            DrawingCommand command = new DrawingCommand("right " + runX);
                            drawingVer.addCommand(command);
                            runX = 0;
                        }
                        if (pixels[0][j] != background) {
                            DrawingCommand command = new DrawingCommand("up 1");
                            drawingVer.addCommand(command);
                            runY++;
                        }
                    }
                    DrawingCommand command = new DrawingCommand("down " + runY + " " + toHex(current[2]));
                    drawingVer.addCommand(command);
                }
                runY = 0;
            } else if (i == 0 && runY > 0) {
                if (current[2] == background) {
                    if (lineChanged) {
                        DrawingCommand command = new DrawingCommand("up " + runY);
                        drawingVer.addCommand(command);
                    } else {
                        i = height - 1;
                    }
                } else {
                    if (firstChar) {
                        if (runX > 0) {
                            DrawingCommand command = new DrawingCommand("right " + runX);
                            drawingVer.addCommand(command);
                            runX = 0;
                        }
                        if (pixels[height - 1][j] != background) {
                            DrawingCommand command = new DrawingCommand("down 1");
                            drawingVer.addCommand(command);
                            runY++;
                        }
                    }
                    DrawingCommand command = new DrawingCommand("up " + runY + " " + toHex(current[2]));
                    drawingVer.addCommand(command);
                }
                runY = 0;
            }
            runX++;
            j++;
            lineChanged = false;
            firstChar = true;
        }
        //Compare the drawings and return the one with the least commands
        if (drawingHor.getCommandsLength() < drawingVer.getCommandsLength()) {
            return drawingHor;
        } else {
            return drawingVer;
        }
    }

    // Render the image into a PNG with the given filename.
    public void toPNG(String filename) {

        BufferedImage im = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < pixels.length; i++)
            for (int j = 0; j < pixels[i].length; j++) {
                im.setRGB(j, i, colours[pixels[i][j]]);
            }

        File f = new File(filename + ".png");
        try {
            ImageIO.write(im, "PNG", f);
        } catch (IOException e) {
            System.out.println("Unable to write image");
            System.exit(1);
        }
    }

    //Changes the colour of a single pixel.
    public void paint(int y, int x, int colour) throws BadCommand {
        try {
            pixels[y][x] = colour;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BadCommand("Tried to paint of out bounds.");
        }
    }
}
