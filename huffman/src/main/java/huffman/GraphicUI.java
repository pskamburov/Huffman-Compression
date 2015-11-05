package huffman;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GraphicUI {
	private static JFrame frame;
	private static JTextArea displayArea;
	private Huffman huffman = new Huffman();


	/**
	 * Display a message to the "logger".
	 *
	 * @param messageToDisplay
	 */
	public static void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(() -> {
			displayArea.append(messageToDisplay);
		});
	}

	public void BuildGUI() {
		frame = new JFrame("Huffman Project");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLayout(new GridBagLayout());
		drawFrequencyTablePanel();
		drawInfoPanel();
		drawCompressionPanel();
		drawExportTree();
		drawImportTree();
		drawDecompressPanel();
		// frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void drawInfoPanel() {
		JPanel pnlLogger = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlLogger.setBorder(BorderFactory.createTitledBorder("Info"));

		constraints.anchor = GridBagConstraints.CENTER;
		displayArea = new JTextArea(10, 35);
		displayArea.setEditable(false);
		pnlLogger.add(new JScrollPane(displayArea));

		constraints.gridx = 0;
		constraints.gridy = 5;
		frame.add(pnlLogger, constraints);

	}

	private void displayThreads() {
		List<AtomicLong> executions = new ArrayList<AtomicLong>(
				ThreadStats.stats.values());
		long sum = 0;
		for (AtomicLong value : executions) {
			sum += value.get();
		}
		for (Entry<String, AtomicLong> entry : ThreadStats.stats.entrySet()) {
			float x = ((float) entry.getValue().get() / (float) sum) * 100;
			displayMessage(entry.getKey() + " ---> " + x + " %\n");
		}
		ThreadStats.stats = new ConcurrentHashMap<String, AtomicLong>();
	}

	private void drawFrequencyTablePanel() {
		JPanel pnlFrequencyTable = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlFrequencyTable.setBorder(BorderFactory
				.createTitledBorder("Frequency table"));
		JLabel lblThreads = new JLabel("Threads:");
		JTextField txtThreads = new JTextField(12);
		JLabel lblFileName = new JLabel("File name:");
		JTextField txtFileName = new JTextField(12);
		JButton btnFrequencyTable = new JButton("Get frequency table");
		btnFrequencyTable.addActionListener((ActionEvent ae) -> {
			int threads = Integer.parseInt(txtThreads.getText());
			long startTime = System.currentTimeMillis();
			if (threads == 1) {
				HashMap<Integer, Integer> table = huffman.oneThread(txtFileName
						.getText());
				long endTime = System.currentTimeMillis();

				NumberFormat formatter = new DecimalFormat("#0.00000");
				displayMessage("Execution time is "
						+ formatter.format((endTime - startTime) / 1000d)
						+ " seconds\n");
				displayMessage(table.toString() + "\n");
			} else {
				ConcurrentHashMap<Integer, AtomicLong> frequencyTable = huffman
						.frequenceTable(threads, txtFileName.getText());

				long endTime = System.currentTimeMillis();

				NumberFormat formatter = new DecimalFormat("#0.00000");
				displayMessage("Execution time is "
						+ formatter.format((endTime - startTime) / 1000d)
						+ " seconds\n");
				displayMessage(frequencyTable.toString() + "\n");
				huffman.setHuffmanTree(new HuffmanTree(frequencyTable));
			}
			displayThreads();

		});
		constraints.gridx = 0;
		constraints.gridy = 0;
		pnlFrequencyTable.add(lblFileName, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		pnlFrequencyTable.add(txtFileName, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		pnlFrequencyTable.add(lblThreads, constraints);
		constraints.gridx = 3;
		constraints.gridy = 0;
		pnlFrequencyTable.add(txtThreads, constraints);
		constraints.gridx = 4;
		constraints.gridy = 0;
		pnlFrequencyTable.add(btnFrequencyTable, constraints);

		constraints.gridx = 0;
		constraints.gridy = 0;
		frame.add(pnlFrequencyTable, constraints);
	}

	private void drawCompressionPanel() {
		JPanel pnlCompression = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlCompression.setBorder(BorderFactory
				.createTitledBorder("Compress File"));
		JLabel lblFileName = new JLabel("Source file:");
		JTextField txtFileName = new JTextField(12);
		JLabel lblCompressFileName = new JLabel("Compressed file:");
		JTextField txtCompressFileName = new JTextField(12);
		JButton btnCompress = new JButton("Compress");
		btnCompress.addActionListener((ActionEvent ae) -> {
			huffman.compressFile(txtFileName.getText(),
					txtCompressFileName.getText());
			displayMessage("Compression completed\n");
		});
		constraints.gridx = 0;
		constraints.gridy = 0;
		pnlCompression.add(lblFileName, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		pnlCompression.add(txtFileName, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		pnlCompression.add(lblCompressFileName, constraints);
		constraints.gridx = 3;
		constraints.gridy = 0;
		pnlCompression.add(txtCompressFileName, constraints);
		constraints.gridx = 4;
		constraints.gridy = 0;
		pnlCompression.add(btnCompress, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		frame.add(pnlCompression, constraints);
	}

	private void drawExportTree() {
		JPanel pnlExportTree = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlExportTree.setBorder(BorderFactory
				.createTitledBorder("Export Huffman tree"));
		JLabel lblFileName = new JLabel("File:");
		JTextField txtFileName = new JTextField(12);
		JButton btnExportTree = new JButton("Export");
		btnExportTree.addActionListener((ActionEvent ae) -> {
			huffman.exportHuffmanTree(txtFileName.getText());
			displayMessage("Tree exported\n");
		});
		constraints.gridx = 0;
		constraints.gridy = 0;
		pnlExportTree.add(lblFileName, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		pnlExportTree.add(txtFileName, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		pnlExportTree.add(btnExportTree, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		frame.add(pnlExportTree, constraints);
	}

	private void drawImportTree() {
		JPanel pnlImportTree = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlImportTree.setBorder(BorderFactory
				.createTitledBorder("Import Huffman tree"));
		JLabel lblFileName = new JLabel("File:");
		JTextField txtFileName = new JTextField(12);
		JButton btnImportTree = new JButton("Import");
		btnImportTree.addActionListener((ActionEvent ae) -> {
			huffman.importHuffmanTree(txtFileName.getText());
			displayMessage("Tree imported\n");
		});
		constraints.gridx = 0;
		constraints.gridy = 0;
		pnlImportTree.add(lblFileName, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		pnlImportTree.add(txtFileName, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		pnlImportTree.add(btnImportTree, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		frame.add(pnlImportTree, constraints);
	}

	private void drawDecompressPanel() {
		JPanel pnlDecompress = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		pnlDecompress.setBorder(BorderFactory
				.createTitledBorder("Decompress File"));
		JLabel lblFileName = new JLabel("Compressed file:");
		JTextField txtCompressedFileName = new JTextField(12);
		JLabel lblCompressFileName = new JLabel("Decompressed file:");
		JTextField txtDecompressedFileName = new JTextField(12);
		JButton btnCompress = new JButton("Decompress");
		btnCompress.addActionListener((ActionEvent ae) -> {
			huffman.decompressFile(txtCompressedFileName.getText(),
					txtDecompressedFileName.getText());
			displayMessage("Decompress completed\n");
		});
		constraints.gridx = 0;
		constraints.gridy = 0;
		pnlDecompress.add(lblFileName, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		pnlDecompress.add(txtCompressedFileName, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		pnlDecompress.add(lblCompressFileName, constraints);
		constraints.gridx = 3;
		constraints.gridy = 0;
		pnlDecompress.add(txtDecompressedFileName, constraints);
		constraints.gridx = 4;
		constraints.gridy = 0;
		pnlDecompress.add(btnCompress, constraints);

		constraints.gridx = 0;
		constraints.gridy = 4;
		frame.add(pnlDecompress, constraints);
	}
}
