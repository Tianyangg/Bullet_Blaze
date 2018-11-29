package main.code.utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//A class that reads the map and stores the position of each element
/*
        0 - empty
        1 - platform
        2 - box
        3 - player
 */

/**
 * A class that read a map from a file
 *      0 - empty
 *      1 - platform
 *      2 - box
 *      3 - player
 *
 *@author Matei Vicovan-Hantascu
 */
public class MapReader {

    /**
     * A method that reads the map from the file
     * The map will be first filled with 4 levels of 0
     * making sure that there will always enough space for
     * the player to jump without getting out of the map
     * @param file
     *          path to the file
     * @return
     *          a Map class defining the map for the game
     */
    public Map readMap(String file){

        ArrayList<Integer[]> map = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> boxPoints = new ArrayList<>();
        int max = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            int i = 0;

            for(String line; (line = br.readLine()) != null; ) {
                Integer[] aux = new Integer[line.length()];
                if(max < line.length()){
                    max = line.length();
                }

                for(int j = 0; j < line.length(); j ++){

                    switch(line.charAt(j)){
                        case '0':
                            aux[j] = 0;
                            break;
                        case '1':
                            aux[j] = 1;
                            break;
                        case '2':
                            aux[j] = 2;
                            boxPoints.add(new Point(j, i + 4));
                            break;
                        case '3':
                            aux[j] = 0;
                            points.add(new Point(j, i + 3));
                            break;
                    }
                }

                map.add(aux);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Point[] spawnPoints = new Point[points.size()];

        int i = 0;
        for(Point p: points){
            spawnPoints[i] = p;
            i++;
        }

        Point[] boxSpawnPoints = new Point[boxPoints.size()];

        int j = 0;
        for(Point p : boxPoints){
            boxSpawnPoints[j] = p;
            j++;
        }

        int[][] grid = new int[map.size() + 4][map.get(0).length];

        for(i = 0; i < 4; i++){
            for(j = 0; j < max; j++){
                grid[i][j] = 0;
            }
        }

        for(i = 4; i < map.size() + 4; i++){
            for(j = 0; j < max; j++){
                grid[i][j] = map.get(i - 4)[j];
            }
        }

        return (new Map(grid, spawnPoints,boxSpawnPoints));
    }
}
