package util;

import core.Node;

/**
 * @author 12130
 * @date 2019/10/30
 * @time 9:18
 */
public class DfsUtil {

    public static void dfs(Node root, StringBuilder path, String[] binaryString) {
        if (root.left == null) {
            binaryString[(root.value & 0xff)] = path.toString();
            return;
        }
        dfs(root.left, path.append("0"), binaryString);
        path.delete(path.length() - 1, path.length());
        dfs(root.right, path.append("1"), binaryString);
        path.delete(path.length() - 1, path.length());
    }
}
