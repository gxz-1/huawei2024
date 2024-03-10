package com.huawei.codecraft;

import java.util.LinkedList;
import java.util.List;

public class PathSearch {
    private static final int[][] MAZE = {
            {0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 0},
            {0, 1, 0, 1, 0, 0},
            {0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0},
            {0, 0, 0, 1, 0, 0}
    };

    private static final int START_X = 1;
    private static final int START_Y = 1;
    private static final int END_X = 4;
    private static final int END_Y = 5;

    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    public static void main(String[] args) {
        List<Integer> path = findPath(MAZE, START_X, START_Y, END_X, END_Y);
        if (path != null) {
            System.out.println("Found path: " + path);
        } else {
            System.out.println("No path found.");
        }
    }

    private static List<Integer> findPath(int[][] maze, int startX, int startY, int endX, int endY) {
        boolean[][] visited = new boolean[maze.length][maze[0].length];
        LinkedList<Integer> path = new LinkedList<>();
        path.add(startX);
        path.add(startY);

        return dfs(maze, visited, path, startX, startY, endX, endY);
    }

    private static List<Integer> dfs(int[][] maze, boolean[][] visited, LinkedList<Integer> path,
                                     int x, int y, int endX, int endY) {
        if (x == endX && y == endY) {
            return new LinkedList<>(path); // Return a new list to avoid modifying the original path
        }
        visited[x][y] = true;
        for (int[] direction : DIRECTIONS) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];
            if (isValidMove(maze, visited, nextX, nextY)) {
                LinkedList<Integer> newPath = new LinkedList<>(path);
                newPath.add(nextX);
                newPath.add(nextY);
                List<Integer> result = dfs(maze, visited, newPath, nextX, nextY, endX, endY);
                if (result != null) {
                    return result;
                }
            }
        }
        visited[x][y] = false; // Backtrack
        path.removeLast();
        path.removeLast();
        return null;
    }

    private static boolean isValidMove(int[][] maze, boolean[][] visited, int x, int y) {
        return x >= 0 && x < maze.length && y >= 0 && y < maze[0].length && maze[x][y] == 0 && !visited[x][y];
    }
}