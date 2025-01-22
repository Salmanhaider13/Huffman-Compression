import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;

class HuffmanCoding {

    static void setQueue(Map<Character, Integer> characterFrequencies, Deque<Character> allCharacters,
            Map<Character, String> characterCodes) {
        Map<Character, Integer> sample = new HashMap<>(characterFrequencies);
        allCharacters.clear();
        char c = '0';
        while (allCharacters.size() != characterFrequencies.size()) {
            int min = Integer.MAX_VALUE;
            for (Map.Entry<Character, Integer> a : sample.entrySet()) {
                if (a.getValue() < min) {
                    min = a.getValue();
                    c = a.getKey();
                }
            }
            sample.remove(c);
            allCharacters.addLast(c);
        }
        for (Character character : allCharacters) {
            characterCodes.put(character, "");
        }
    }

    static Map<Character, Integer> calculateFrequency(String input) {
        Map<Character, Integer> characterFrequencies = new HashMap<>();
        for (char c : input.toCharArray()) {
            characterFrequencies.put(c, characterFrequencies.getOrDefault(c, 0) + 1);
        }
        return characterFrequencies;
    }

    public static void main(String[] args) {
        Map<Character, Integer> characterFrequencies = new HashMap<>();
        Deque<Character> allCharacters = new ArrayDeque<>();
        Map<Character, String> characterCodes = new HashMap<>();
        Tree huffmanTree = new Tree();
        Scanner sc = new Scanner(System.in);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(null);
        BufferedReader br = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                System.out.println("Selected file: " + fileChooser.getSelectedFile().getName());
            } catch (FileNotFoundException e) {
                System.err.println("Error: File not found.");
                return;
            }
        } else {
            System.err.println("Error: No file selected.");
            return;
        }

        String input = "";
        String sample;
        try {
            while ((sample = br.readLine()) != null) {
                input += sample;
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        characterFrequencies = calculateFrequency(input);
        setQueue(characterFrequencies, allCharacters, characterCodes);
        huffmanTree.buildHuffmanTree(characterFrequencies);
        huffmanTree.generateCodes(characterCodes);
        huffmanTree.display(huffmanTree.root);
        for (Map.Entry<Character, String> c : characterCodes.entrySet()) {
            System.out.println(c.getKey() + "->" + c.getValue());
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("Compressed_" + fileChooser.getSelectedFile().getName());
            int choice = 0;
            String encodeString = "";
            do {
                System.out.println("Enter 1 for encode the input String");
                System.out.println("Enter 2 for decode the input String");
                System.out.println("Enter 3 for Exit\n");
                System.out.println("Enter Your Choice");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        if (encodeString.isEmpty()) {
                            for (Character c : input.toCharArray()) {
                                encodeString += characterCodes.get(c);
                            }
                            // Convert the binary string to bytes
                            byte[] bytes = new byte[(encodeString.length() + 7) / 8];
                            for (int i = 0; i < encodeString.length(); i += 8) {
                                String byteString = encodeString.substring(i,
                                        Math.min(i + 8, encodeString.length()));
                                bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
                            }
                            // Write the bytes to the output stream
                            fos.write(bytes);
                            fos.flush();
                            System.out.println("FILE COMPRESSION SUCCESSFUL");
                        }
                        break;
                    case 2:
                        try {
                            if (!encodeString.isEmpty()) {
                                System.out.println("Decoded Text : " + huffmanTree.decode(encodeString));
                            } else {
                                throw new Exception("Please First Encode The Text");
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case 3:
                        System.out.println("Thank You");
                        break;
                    default:
                        System.out.println("Enter a Valid Choice");
                        break;
                }
            } while (choice != 3);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error closing file output stream: " + e.getMessage());
                }
            }
            sc.close();
        }
    }
}

class Tree {
    Node root;

    class Node {
        char data;
        int frequency;
        Node leftNode;
        Node rightNode;

        Node(char data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }
    }

    void buildHuffmanTree(Map<Character, Integer> characterFrequencies) {
        Deque<Node> nodes = new ArrayDeque<>();
        for (Map.Entry<Character, Integer> c : characterFrequencies.entrySet()) {
            nodes.addLast(new Node(c.getKey(), c.getValue()));
        }
        while (nodes.size() > 1) {
            Node leftNode = nodes.poll();
            Node rightNode = nodes.poll();
            Node parentNode = new Node((char) Integer.MIN_VALUE, leftNode.frequency + rightNode.frequency);
            parentNode.leftNode = leftNode;
            parentNode.rightNode = rightNode;
            nodes.addLast(parentNode);
        }
        root = nodes.poll();
    }

    void display(Node root) {
        if (root == null) {
            return;
        }

        display(root.leftNode);
        System.out.print(root.data);
        display(root.rightNode);
    }

    String traverse(Node root, char data, String code) {
        if (root == null) {
            return "";
        }
        if (root.data == data) {
            return code;
        }
        String leftCode = traverse(root.leftNode, data, code + "0");
        if (!leftCode.isEmpty()) {
            return leftCode;
        }

        String rightCode = traverse(root.rightNode, data, code + "1");
        if (!rightCode.isEmpty()) {
            return rightCode;
        }

        return "";
    }

    void generateCodes(Map<Character, String> characterCodes) {
        for (Map.Entry<Character, String> a : characterCodes.entrySet()) {
            characterCodes.put(a.getKey(), traverse(root, a.getKey(), a.getValue()));
        }
    }

    String decode(String encodString) {
        String decodedText = "";
        Node currentNode = this.root;

        for (char bit : encodString.toCharArray()) {
            if (bit == '0') {
                currentNode = currentNode.leftNode;
            } else if (bit == '1') {
                currentNode = currentNode.rightNode;
            }

            if (currentNode.leftNode == null && currentNode.rightNode == null) {
                decodedText += currentNode.data;
                currentNode = root;
            }
        }
        return decodedText;
    }
}
