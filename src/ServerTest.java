import javax.swing.JFrame;

public class ServerTest {

	public static void main(String[] args) {
		Server sunny = new Server("192.168.1.64");
		sunny.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sunny.startRunning();
	}
}
