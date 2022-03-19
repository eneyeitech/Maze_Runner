package maze;

import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main {


    public static void main(String[] args) {
        final boolean debugMode = false;



        Scanner scanner = new Scanner(System.in);
        System.out.println("Please, enter the size of a maze");
        int height = scanner.nextInt();
        int weight = scanner.nextInt();

        Maze maze = new Maze(height, weight, debugMode);
        maze.generate();
    }
}

class Maze {
    private final int height;
    private final int weight;
    private final boolean debugMode;
    private final Random random;
    final String w = "\u2588\u2588";
    final String o = "  ";

    public Maze(int height, int weight, boolean debugMode) {
        this.height = height;
        this.weight = weight;
        this.debugMode = debugMode;
        this.random = new Random();
    }

    public void generate() {
        int heightEdges = (int) Math.ceil(((double) height - 2) / 2);
        int weightEdges = (int) Math.ceil(((double) weight - 2) / 2);
        if (debugMode) {
            System.out.println("Матрица узлов " + heightEdges + ":" + weightEdges);
        }

        int[][] adjacencyMatrix = new int[heightEdges * weightEdges][heightEdges * weightEdges];
        // заполним нолями
        for (
                int i = 0;
                i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // вычисляем узлы
//        Random random = new Random(99999);
        int node = 0;
        int edge;
        for (
                int i = 0;
                i < adjacencyMatrix.length - 1; i++) {
            //соседний вправо узел
            edge = random.nextInt(height * weight) + 1;
            //елси не выходим за пределы строки массива
            if (node + 1 < ((node / weightEdges) + 1) * weightEdges) {
                adjacencyMatrix[node][node + 1] = edge;
                adjacencyMatrix[node + 1][node] = edge;
            }
            // соседний вниз узел
            edge = random.nextInt(height * weight) + 1;
            // если не выходим на пределы строки массива
            if (node + weightEdges < adjacencyMatrix.length) {
                adjacencyMatrix[node][node + weightEdges] = edge;
                adjacencyMatrix[node + weightEdges][node] = edge;
            }
            node++;
        }
        // печать матрицы
        if (debugMode) {
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    System.out.print(adjacencyMatrix[i][j] + ",");
                }
                System.out.println();
            }
        }

        // минимальное связующее дерево Прима
        int[][] minSpanTree = new int[adjacencyMatrix.length][adjacencyMatrix.length];
        Set<Integer> addedNodes = new HashSet<>();
        addedNodes.add(0);
        int way = 0;
        int nextNode = 0;
        int currentNode = 0;

        while (addedNodes.size() < adjacencyMatrix.length) {
            int minValues = adjacencyMatrix.length * adjacencyMatrix.length;


            for (int eachNode : addedNodes) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    // проверим кажое ребро ноды в наборе на значение, что оно не ноль и что оно не в наборе
                    if (adjacencyMatrix[eachNode][j] < minValues && adjacencyMatrix[eachNode][j] > 0) {
                        if (!addedNodes.contains(j)) {
                            minValues = adjacencyMatrix[eachNode][j];
                            currentNode = eachNode;
                            nextNode = j;
                        }
                    }
                }
            }

            // вычислим минимальный путь
            way += adjacencyMatrix[currentNode][nextNode];
            // выведем ребра
            if (debugMode) {
                System.out.println(currentNode + ":" + nextNode + " " + adjacencyMatrix[currentNode][nextNode]);
            }
            // добавим найденный узел в набор и в матрицу минимального дерева
            addedNodes.add(nextNode);
            minSpanTree[currentNode][nextNode] = 1;
            minSpanTree[nextNode][currentNode] = 1;

        }
        // напечатаем матрицу минимального дерева
        if (debugMode) {
            System.out.println("Minimum way:" + way);
            for (int i = 0; i < minSpanTree.length; i++) {
                for (int j = 0; j < minSpanTree.length; j++) {
                    System.out.print(minSpanTree[i][j] + ",");
                }
                System.out.println();
            }
        }

        // подготовка лабиринта
        // сначала заполним его стенами
        int[][] maze = new int[height][weight];
        for (
                int i = 0;
                i < height; i++) {
            for (int j = 0; j < weight; j++) {
                maze[i][j] = 1;
            }
        }
        // вход в лабиринт всегда там где нода 0
        maze[1][0] = 0;

        int currentEdge = 0;
        int mazeRow = 1;
        int mazeCol = 1;
        while (currentEdge < heightEdges * weightEdges) {
            if (currentEdge + 1 < (currentEdge / weightEdges + 1) * weightEdges && minSpanTree[currentEdge][currentEdge + 1] == 1) {
                maze[mazeRow][mazeCol] = 0;
                maze[mazeRow][mazeCol + 1] = 0;
                maze[mazeRow][mazeCol + 2] = 0;
            }
            if (currentEdge + weightEdges < heightEdges * weightEdges) {
                if (minSpanTree[currentEdge][currentEdge + weightEdges] == 1) {
                    maze[mazeRow][mazeCol] = 0;
                    maze[mazeRow + 1][mazeCol] = 0;
                    maze[mazeRow + 2][mazeCol] = 0;
                }
            }
            if (currentEdge + 1 <= (currentEdge / weightEdges + 1) * weightEdges - 1) {
                mazeCol += 2;
            } else {
                mazeRow += 2;
                mazeCol = 1;
            }

            currentEdge++;
            // если нода была последней то сразу рисуем выход рядом с ней
            if (currentEdge == heightEdges * weightEdges) {
                maze[mazeRow - 2][weight - 1] = 0;
            }
        }

        // печать лабиритна
        for (
                int i = 0;
                i < height; i++) {
            for (int j = 0; j < weight; j++) {
                if (maze[i][j] == 1) {
                    System.out.print(w);
                } else {
                    System.out.print(o);
                }
            }
            System.out.println();
        }
    }
}

