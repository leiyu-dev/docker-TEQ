import java.io.File;

public class ListFilesExample {
    public static void main(String[] args) {
        // 指定目标文件夹路径
        String folderPath = "./docker";
        String projectPath = System.getProperty("user.dir");
        System.out.println("Project path: " + projectPath);
        // 创建文件夹的 File 对象
        File folder = new File(folderPath);

        // 检查文件夹是否存在并且是一个目录
        if (folder.exists() && folder.isDirectory()) {
            // 获取文件夹下的所有文件和文件夹列表
            File[] files = folder.listFiles();

            if (files != null) {
                System.out.println("Files in folder: " + folderPath);
                for (File file : files) {
                    // 仅输出文件名
                    if (file.isFile()) {
                        System.out.println(file.getName());
                    }
                }
            } else {
                System.out.println("The folder is empty or cannot be read.");
            }
        } else {
            System.out.println("The specified path does not exist or is not a directory.");
        }
    }
}
