package de.uka.ipd.sdq.ByCounter.test;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import de.uka.ipd.sdq.ByCounter.test.helpers.StatefulRunnable;
import de.uka.ipd.sdq.ByCounter.test.helpers.SynchronizedTestSubject;

public class StateGUI {

	public JFrame frame;
	private final Action action = new SwingAction();
	private StatefulRunnable statefulRunnable;
	private Thread executeThread;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StateGUI window = new StateGUI(Thread.currentThread(), new SynchronizedTestSubject());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @param executeThread 
	 */
	public StateGUI(Thread executeThread, StatefulRunnable statefulRunnable) {
		this.executeThread = executeThread;
		this.statefulRunnable = statefulRunnable;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnNextState = new JButton("Next State");
		btnNextState.setAction(action);
		frame.getContentPane().add(btnNextState, BorderLayout.CENTER);
	}

	private class SwingAction extends AbstractAction {
		/** Version. */
		private static final long serialVersionUID = 1L;
		public SwingAction() {
			putValue(NAME, "Next State");
			putValue(SHORT_DESCRIPTION, "Trigger next state.");
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("lol");
			executeThread.interrupt();
			statefulRunnable.nextState();
		}
	}
}
