package main.code.Networking.utils;

import main.code.utils.*;
import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class PackDigest {
    public static byte[] bullet(Bullet b, String typ) {
        byte[] tp = typ.getBytes();
        byte[] xPos = Serialization.shortToByte(b.getxPos());
        byte[] yPos = Serialization.shortToByte(b.getyPos());
        byte[] originX = Serialization.shortToByte(b.originX);
        byte[] sourcePlayerId = new byte[]{b.getSourcePlayerId()};
        byte[] dmg = new byte[]{b.getDmg()};
        byte[] isDisplayed = b.isDisplayed() ? new byte[]{1} : new byte[]{0};
        byte[] range = Serialization.doubleToByte(b.getRange());
        byte[] direction = Serialization.shortToByte(b.direction);
        short packSize = (short) (tp.length + 2 * 4 + 8 + 1 + 1 + 1 + 1);
        return Serialization.concat(packSize, tp, xPos, yPos, originX, sourcePlayerId, dmg, isDisplayed, range, direction,new byte[]{b.getWeaponID()});
    }

    public static Bullet bullet(byte[] p, String typ) {
        int offset = typ.length();
        short xpos = Serialization.byteToShort(Arrays.copyOfRange(p, offset, offset + 2));
        offset += 2;
        short ypos = Serialization.byteToShort(Arrays.copyOfRange(p, offset, offset + 2));
        offset += 2;
        short originX = Serialization.byteToShort(Arrays.copyOfRange(p, offset, offset + 2));
        offset += 2;
        byte sourcePlayerId = p[offset++];
        byte dmg = p[offset++];
        boolean isDisplayed = p[offset++] != 0;
        double range = Serialization.bytesToDouble(Arrays.copyOfRange(p, offset, offset + 8));
        offset += 8;
        short direction = Serialization.byteToShort(Arrays.copyOfRange(p, offset, offset + 2));
        offset+=2;
        byte weaponID = p[offset];
        return new Bullet(xpos,ypos,originX,sourcePlayerId,dmg, isDisplayed,range, direction,weaponID);
        }

    public static byte[] bullets(ArrayList<Bullet> bulletA, String typ) {
        byte[] tp = typ.getBytes();
        byte[][] toS = new byte[bulletA.size()][];
        byte i = 0;
        short packSize = (short) tp.length;
        for (Bullet b : bulletA) {
            toS[i++] = PackDigest.bullet(b, "");
            packSize += toS[i - 1].length;
        }
        byte[] toSend = new byte[packSize];
        int offset = 0;
        for (byte[] arr : toS) {
            for (byte b : arr) {
                toSend[offset++] = b;
            }
        }
        packSize++;
        return Serialization.concat(packSize, tp, new byte[]{i}, toSend);
    }

    public static ArrayList<Bullet> bullets(byte[] pack, String typ) {
        int offset = typ.length();
        byte n = pack[offset++];
        ArrayList<Bullet> b = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            b.add(PackDigest.bullet(Arrays.copyOfRange(pack, offset, offset + 20), ""));
            offset += 20;
        }
        return b;
    }

    public static Player player(byte[] pack, String typ) {
        int offset = typ.length();
        byte id = pack[offset++];
        byte hp = pack[offset++];
        byte char_id = pack[offset++];
        int movement = (int)pack[offset++];
        boolean sprint = pack[offset++] == 1;
        int score = Serialization.fromByteArray(Arrays.copyOfRange(pack,offset,offset+4));
        offset+=4;
        byte roundsWon = pack[offset++];
        boolean ready = pack[offset++] == 1;
        short xPos = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        short yPos = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        boolean jumpState = pack[offset++] != 0;
        boolean fallState = pack[offset++] != 0;
        short initial_y = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        byte currentWeapon = pack[offset++];
        short currentWeaponbullets = Serialization.byteToShort(Arrays.copyOfRange(pack,offset,offset+2));
        offset+=2;
        short shootingDirection = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        byte nameLength = pack[offset++];
        String name = new String(Arrays.copyOfRange(pack, offset, offset + nameLength));
        offset += nameLength;
        byte nrWeapons = pack[offset++];
        ArrayList<Pair<Byte, Short>> weapons = new ArrayList<>();
        int i = offset;
        int j = i;
        for (; i < nrWeapons +nrWeapons*2 + j; ) {
            byte wid = pack[i++];
            short amm = Serialization.byteToShort(Arrays.copyOfRange(pack,i,i+2));
            i+=2;
            weapons.add(new Pair<>(wid, amm));
        }
        offset = i;
        ArrayList<Bullet> bullets = PackDigest.bullets(Arrays.copyOfRange(pack, offset, pack.length), "");
        offset = pack[offset] * 19 + offset + 1;
        byte trakedWeaponsNr = pack[offset++];
        LinkedList<Byte> trakedWeapons = new LinkedList<>();
        for (i = 0; i < trakedWeaponsNr; i++) {
            trakedWeapons.add(pack[offset++]);
        }
        return new Player(id, xPos, yPos, hp, name, weapons, bullets, trakedWeapons, jumpState, fallState, initial_y, currentWeapon,currentWeaponbullets, shootingDirection,sprint,score,roundsWon,ready,movement,char_id);
    }
    public static byte getMovement(int x){
        switch (x){
            case 0: return 0;
            case 1: return 1;
            case 2: return 2;
            case 3: return 3;
            case 4: return 4;
            case 5: return 5;
            default: return 0;
        }
    }
    public static byte[] player(Player p, String typ) {
        byte[] type = typ.getBytes();
        byte[] hp = new byte[]{p.getHp()};
        byte[] sprint = new byte[]{(byte) (p.isSprinting() ? 1 : 0)};
        byte[] score = Serialization.intToByteArray(p.getScore());
        byte[] movement = new byte[]{getMovement(p.getStateOfMovement())};
        byte[] roundsWon = new byte[]{p.getRoundsWon()};
        byte[] ready = new byte[]{(byte)(p.ready ? 1 : 0)};
        byte[] id = new byte[]{p.getId()};
        byte[] xpos = Serialization.shortToByte(p.getXPos());
        byte[] ypos = Serialization.shortToByte(p.getYPos());
        ArrayList<Pair<Byte, Short>> weapons = p.getWeapons();
        ArrayList<Bullet> bullets = p.getBulletsFired();
        byte[] nrBullets = new byte[]{(byte) (bullets.size())};
        byte[] nrWeapons = new byte[]{(byte) (weapons.size())};
        byte[] weaponsBytes = new byte[weapons.size() * 3];
        byte i = 0;
        byte[] aux;
        for (Pair<Byte,Short> pair : weapons) {
            weaponsBytes[i++] = (byte) pair.getKey();
            aux = Serialization.shortToByte(pair.getValue());
            weaponsBytes[i++] = aux[0];
            weaponsBytes[i++] = aux[1];
        }
        byte[] bPack = PackDigest.bullets(new ArrayList<>(p.getBulletsFired()), "");
        byte[] weaponEntryTrackerBytes = new byte[p.getWeaponEntry().size()];
        i = 0;
        for (byte w : p.getWeaponEntry()) {
            weaponEntryTrackerBytes[i++] = w;
        }
        byte[] name = p.name.getBytes();
        byte nameLength = (byte) name.length;
        byte[] jumpState = new byte[]{(byte) (p.isJumping() ? 1 : 0)};
        byte[] fallState = new byte[]{(byte) (p.isFalling() ? 1 : 0)};
        byte[] initial_y = Serialization.shortToByte(p.getInitial_y());
        byte[] currentWeapon = new byte[]{p.getCurrentWeapon().getKey()};
        byte[] currWeaponAmmo = Serialization.shortToByte(p.getCurrentWeapon().getValue());
        byte[] shootingDirection = Serialization.shortToByte(p.getShootingDirection());
        short packSize = (short) (type.length + 2 + 4 + 2 + 1+1 +1+4+1+1+1+1+ weapons.size() * 2 + 2 + bPack.length + weaponEntryTrackerBytes.length + 1 + 1 + nameLength + 2 + 2 + 1 + 2);
        byte[] toSend = Serialization.concat(packSize, type, id, hp,new byte[]{p.get_charId()},movement,sprint,score,roundsWon,ready, xpos, ypos, jumpState, fallState, initial_y, currentWeapon,currWeaponAmmo, shootingDirection, new byte[]{nameLength}, name, nrWeapons, weaponsBytes, bPack, new byte[]{i}, weaponEntryTrackerBytes);
        return toSend;
    }




    private static byte get_curr(ArrayList<Pair<Byte, Byte>> weapons, Pair<Byte, Byte> currentWeapon) {
        for (byte i = 0; i < weapons.size(); i++) {
            if (currentWeapon.getKey() == weapons.get(i).getKey()) return i;
        }
        return -1;
    }

    public static byte[] players(java.util.List<Player> players, String typ) {
        byte[] type = typ.getBytes();
        short packetLength = (short) type.length;
        byte[][] playersBytes = new byte[players.size()][];
        byte[] aux;
        int i = 0;
        byte playersSize = 0;
        for (Player p : players) {
            aux = PackDigest.player(p, "");
            playersBytes[i++] = aux;
            packetLength += aux.length;
            playersSize++;
        }
        byte[] playersB = new byte[packetLength - type.length + playersSize*2];
        i = 0;
        int j = 0;
        int k = 0;
        byte[] s;
        for (byte[] row : playersBytes) {
            s = Serialization.shortToByte((short) row.length);
            playersB[k++] =s[0];
            playersB[k++] =s[1];
            j = 0;
            for (byte item : row) {
                playersB[k] = playersBytes[i][j];
                j++;
                k++;
            }
            i++;
        }
        packetLength += 1;
        packetLength += playersSize*2;
        byte[] toSend = Serialization.concat(packetLength, type, new byte[]{playersSize}, playersB);
        return toSend;
    }

    public static ArrayList<Player> players(byte[] pack, String typ) {
        int offset = typ.length();
        byte nrPlayers = pack[offset++];
        Player player;
        short size;
        ArrayList<Player> players = new ArrayList<>();
        for (int j = 0; j < nrPlayers; j++) {
            size = Serialization.byteToShort(Arrays.copyOfRange(pack,offset,offset+2));
            offset+=2;
            player = player(Arrays.copyOfRange(pack, offset, offset + size), "");
            players.add(player);
            offset += size;
        }

        return players;
    }

    public static byte[] map(Map map, String typ) {
        byte[] type = typ.getBytes();
        int[][] mesh = map.getGrid();
        Point[] spawns = map.getSpawns();
        Point[] boxes = map.getBoxSpawns();
        int i = 0;
        int nrPos = 0;
        int k = 0;
        for (int[] row : mesh) {
            int j = 0;
            for (int item : row) {
                nrPos++;
            }
        }
        int rowsize = 0;
        byte[] meshBytes = new byte[nrPos];
        for (int[] row : mesh) {
            int j = 0;
            for (int item : row) {
                meshBytes[k] = (byte) (mesh[i][j] == 1 ? 1 : mesh[i][j] == 2 ? 2 : mesh[i][j] == 0 ? 0 : 0);
                k++;
                j++;
            }
            rowsize = j;
            i++;
        }
        byte[] spawnsBytes = new byte[spawns.length * 8];
        k = 0;
        byte nrSpawns = 0;
        i = 0;
        for (Point p : spawns) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            byte[] xb = Serialization.intToByteArray(x);
            byte[] yb = Serialization.intToByteArray(y);
            for (byte a : xb) {
                spawnsBytes[k] = a;
                k++;
            }
            for (byte a : yb) {
                spawnsBytes[k] = a;
                k++;
            }
            nrSpawns++;
        }
        byte[] boxesBytes = new byte[boxes.length * 8];
        k = 0;
        byte nrBoxes = 0;
        i = 0;
        for (Point p : boxes) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            byte[] xb = Serialization.intToByteArray(x);
            byte[] yb = Serialization.intToByteArray(y);
            for (byte a : xb) {
                boxesBytes[k] = a;
                k++;
            }
            for (byte a : yb) {
                boxesBytes[k] = a;
                k++;
            }
            nrBoxes++;
        }
        byte[] nrPosBytes = Serialization.intToByteArray(nrPos);
        short packetLength = (short) (type.length + 4 + 4 + meshBytes.length + 1 + spawnsBytes.length + 1 + boxesBytes.length);
        byte[] toSend = Serialization.concat(packetLength, type, Serialization.intToByteArray(rowsize), nrPosBytes, meshBytes, new byte[]{nrSpawns}, new byte[]{nrBoxes}, spawnsBytes, boxesBytes);
        return toSend;
    }

    public static Map map(byte[] packet, String typ) {
        int offset = typ.length();
        int rowSize = Serialization.fromByteArray(Arrays.copyOfRange(packet, offset, offset + 4));
        offset += 4;
        int meshSize = Serialization.fromByteArray(Arrays.copyOfRange(packet, offset, offset + 4));
        offset += 4;
        int colSize = meshSize / rowSize;
        int k = offset;
        int i = 0;
        int j = 0;
        int[][] grid = new int[colSize][rowSize];
        for (; k < meshSize + offset; ) {
            int aux = k;
            j = 0;
            for (; k < aux + rowSize; k++, j++) {
                grid[i][j] = packet[k];
            }
            i++;
        }
        offset = k;
        byte nrSpawns = packet[offset++];
        byte nrBoxes = packet[offset++];
        k = offset;
        Point[] spawns = new Point[nrSpawns];
        j = 0;
        for (; k < nrSpawns * 8 + offset; ) {
            int x = Serialization.fromByteArray(Arrays.copyOfRange(packet, k, k + 4));
            k += 4;
            int y = Serialization.fromByteArray(Arrays.copyOfRange(packet, k, k + 4));
            k += 4;
            spawns[j++] = new Point(x, y);
        }
        Point[] boxes = new Point[nrBoxes];
        offset = k;
        j = 0;
        for (; k < nrBoxes * 8 + offset; ) {
            int x = Serialization.fromByteArray(Arrays.copyOfRange(packet, k, k + 4));
            k += 4;
            int y = Serialization.fromByteArray(Arrays.copyOfRange(packet, k, k + 4));
            k += 4;
            boxes[j++] = new Point(x, y);
        }
        return new Map(grid, spawns, boxes);
    }

    public static byte[] box(Box box) {
        byte[] xPos = Serialization.shortToByte(box.getXPos());
        byte[] yPos = Serialization.shortToByte(box.getYPos());
        byte[] hp = new byte[]{box.getHp()};
        byte[] contentsOfBox;
        byte[] aux;
        if(box.getContentsOfBox() != null) {
            if (box.getContentsOfBox().getKey() != null || box.getContentsOfBox().getValue() != null) {
                aux = Serialization.shortToByte(box.getContentsOfBox().getValue());
                contentsOfBox = new byte[]{box.getContentsOfBox().getKey(),aux[0],aux[1]};
            } else contentsOfBox = new byte[]{GameConstants.NO_WEAPON_ID, 0,0};
        }else{
            contentsOfBox = new byte[]{GameConstants.NO_WEAPON_ID, 0,0};
        }
        byte[] isDestroyed = new byte[]{(byte) (box.isDestroyed() ? 1 : 0)};
        short packetLength = 2 * 3 + 1+1 + 1;
        return Serialization.concat(packetLength, xPos, yPos, hp, contentsOfBox, isDestroyed);
    }

    public static Box box(byte[] pack) {
        int offset = 0;
        short xPos = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        short yPos = Serialization.byteToShort(Arrays.copyOfRange(pack, offset, offset + 2));
        offset += 2;
        byte hp = pack[offset++];
        byte wId = pack[offset++];
        short wVal = Serialization.byteToShort(Arrays.copyOfRange(pack,offset,offset+2));
        offset+=2;
        Pair<Byte, Short> content = new Pair<>(wId, wVal);
        boolean isDestroyed = pack[offset++] != 0;
        return new Box(xPos, yPos, hp, content, isDestroyed);
    }

    public static ArrayList<Box> boxes(byte[] bytes, String updateboxes) {
        int offset = updateboxes.length();
        ArrayList<Box> boxes = new ArrayList<>();
        byte nrBoxes = bytes[offset++];
        Box x;
        int i = offset;
        for (; i < nrBoxes * 9 + offset; i += 9) {
            x = PackDigest.box(Arrays.copyOfRange(bytes, i, i + 9));
            boxes.add(x);
        }
        return boxes;
    }

    public static byte[] boxes(ArrayList<Box> boxes, String s) {

        byte nrBoxes = (byte) boxes.size();
        byte[] result = new byte[nrBoxes * 9 + 1];
        byte[] aux;
        result[0] = nrBoxes;
        int k = 1;
        for (Box b : boxes) {
            aux = PackDigest.box(b);
            for (byte x : aux) {
                result[k++] = x;
            }
        }
        return Serialization.concat((short) (result.length + s.length()), s.getBytes(), result);
    }
}
