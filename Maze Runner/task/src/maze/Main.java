package maze;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
public class Main {
    public static void main(String[] args) {
        // Many thanks to Mariano for the idea of using the debugMode flag
        final boolean debugMode = false;
        Maze maze = null;
        String fileName;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mainMenu(maze != null);
            int action = scanner.nextInt();
            switch (action) {
                case 1:
                    System.out.println("Enter the size of a new maze");
                    int mazeSize = scanner.nextInt();
                    maze = new Maze(mazeSize, debugMode);
                    maze.generate();
                    maze.displayMaze();
                    break;
                case 2:
                    scanner.nextLine();
                    System.out.println("Enter the file name:");
                    fileName = scanner.nextLine();
                    try {
                        maze = (Maze) Maze.loadMaze(fileName);
                        System.out.println("Load was successful!");
                    } catch (Exception e) {
                        System.out.println("Cannot load the maze. It has an invalid format");
                        //e.printStackTrace();
                    }
                    break;
                case 3:
                    if (maze == null) {
                        System.out.println("Incorrect option. Please try again");
                    } else {
                        scanner.nextLine();
                        System.out.println("Enter the file name:");
                        fileName = scanner.nextLine();
                        try {
                            Maze.saveMaze(maze, fileName);
                            System.out.println("Save was successful!");
                        } catch (Exception e) {
                            System.out.println("ERROR! Save NOT successful. " + e);
                        }
                    }
                    break;
                case 4:
                    assert maze != null;
                    maze.displayMaze();
                    break;
                case 0:
                    System.out.println("Bye !");
                    System.exit(0);
                default:
                    System.out.println("Incorrect option. Please try again");
                    break;
            }
        }
    }

    public static void mainMenu(boolean hasMaze) {
        System.out.println("=== Menu ===\n" +
                "1. Generate a new maze\n" +
                "2. Load a maze");
        if (hasMaze) {
            System.out.println("3. Save the maze\n" +
                    "4. Display the maze");
        }
        System.out.println("0. Exit");
    }
}

class Maze implements Serializable {
    private final int mazeSize;
    private final boolean debugMode;
    private int[][] minSpanTree;
    private int[][] maze; // the array of the maze to print
    final String WALL = "\u2588\u2588";
    final String EMPTY = "  ";

    public Maze(int mazeSize, boolean debugMode) {
        this.mazeSize = mazeSize;
        this.debugMode = debugMode;
    }

    public void generate() {
        int heightEdges = (int) Math.ceil(((double) mazeSize - 2) / 2);
        int weightEdges = (int) Math.ceil(((double) mazeSize - 2) / 2);
        if (debugMode) {
            System.out.println("Матрица узлов " + heightEdges + ":" + weightEdges);
        }

        int[][] adjacencyMatrix = new int[heightEdges * weightEdges][heightEdges * weightEdges];
        // Fill 0
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // Calculating nodes
        Random random = new Random();
        int node = 0;
        int edge;
        for (int i = 0; i < adjacencyMatrix.length - 1; i++) {
            // The adjacent right node
            edge = random.nextInt(mazeSize * mazeSize) + 1;
            // Let's check if we are not going out of the array
            if (node + 1 < ((node / weightEdges) + 1) * weightEdges) {
                adjacencyMatrix[node][node + 1] = edge;
                adjacencyMatrix[node + 1][node] = edge;
            }
            // The adjacent lower node
            edge = random.nextInt(mazeSize * mazeSize) + 1;
            // Let's check if we are not going out of the array
            if (node + weightEdges < adjacencyMatrix.length) {
                adjacencyMatrix[node][node + weightEdges] = edge;
                adjacencyMatrix[node + weightEdges][node] = edge;
            }
            node++;
        }
        // Print matrix in debug mode
        if (debugMode) {
            for (int[] matrix : adjacencyMatrix) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    System.out.print(matrix[j] + ",");
                }
                System.out.println();
            }
        }

        // Minimum spanning tree Pims
        minSpanTree = new int[adjacencyMatrix.length][adjacencyMatrix.length];
        Set<Integer> addedNodes = new HashSet<>();
        addedNodes.add(0);
        int way = 0;
        int nextNode = 0;
        int currentNode = 0;

        while (addedNodes.size() < adjacencyMatrix.length) {
            int minValues = adjacencyMatrix.length * adjacencyMatrix.length;


            for (int eachNode : addedNodes) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    // Check each node in the set for the value that it is not zero and that it is not in the set
                    if (adjacencyMatrix[eachNode][j] < minValues && adjacencyMatrix[eachNode][j] > 0) {
                        if (!addedNodes.contains(j)) {
                            minValues = adjacencyMatrix[eachNode][j];
                            currentNode = eachNode;
                            nextNode = j;
                        }
                    }
                }
            }

            // Calculating minimal tree
            way += adjacencyMatrix[currentNode][nextNode];
            // Print nodes in debug mode
            if (debugMode) {
                System.out.println(currentNode + ":" + nextNode + " " + adjacencyMatrix[currentNode][nextNode]);
            }
            // Adding the found node to the set and to the minimal tree matrix
            addedNodes.add(nextNode);
            minSpanTree[currentNode][nextNode] = 1;
            minSpanTree[nextNode][currentNode] = 1;

        }

        // Print minimum tree in debug mode
        if (debugMode) {
            System.out.println("Minimum way:" + way);
            for (int[] ints : minSpanTree) {
                for (int j = 0; j < minSpanTree.length; j++) {
                    System.out.print(ints[j] + ",");
                }
                System.out.println();
            }
        }

        // Prepare maze
        // Lets fill it walls (1 = wall)
        maze = new int[mazeSize][mazeSize];
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                maze[i][j] = 1;
            }
        }
        // The entrance to the maze is always where be node #0
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
            // if the node was the last then the exit next to it
            if (currentEdge == heightEdges * weightEdges) {
                maze[mazeRow - 2][mazeSize - 1] = 0;
            }
        }
    }

    // Print maze
    public void displayMaze() {
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                if (maze[i][j] == 1) {
                    System.out.print(WALL);
                } else {
                    System.out.print(EMPTY);
                }
            }
            System.out.println();
        }
    }

    // Saving the object using serialization
    public static void saveMaze(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    // Loading (deserializing) an object
    public static Object loadMaze(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}