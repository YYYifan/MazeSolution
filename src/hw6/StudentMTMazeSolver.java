package hw6;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hw6.SkippingMazeSolver.SolutionFound;

/**
 * This file needs to hold your solver to be tested. You can alter the class to
 * extend any class that extends MazeSolver. It must have a constructor that
 * takes in a Maze. It must have a solve() method that returns the datatype
 * List<Direction> which will either be a reference to a list of steps to take
 * or will be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver {

	public StudentMTMazeSolver(Maze maze) {
		super(maze);
	}

	public List<Direction> solve() {
		// TODO: Implement your code here

		if (maze.getWidth() >= 0 && maze.getWidth() <= 200) {
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice ch;

			try {
				choiceStack.push(firstChoice(maze.getStart()));
				while (!choiceStack.isEmpty()) {
					ch = choiceStack.peek();
					if (ch.isDeadend()) {
						// backtrack.
						choiceStack.pop();
						if (!choiceStack.isEmpty())
							choiceStack.peek().choices.pop();
						continue;
					}
					choiceStack.push(follow(ch.at, ch.choices.peek()));
				}
				// No solution found.
				return null;
			} catch (SolutionFound e) {
				Iterator<Choice> iter = choiceStack.iterator();
				LinkedList<Direction> solutionPath = new LinkedList<Direction>();
				while (iter.hasNext()) {
					ch = iter.next();
					solutionPath.push(ch.choices.peek());
				}

				if (maze.display != null)
					maze.display.updateDisplay();
				return pathToFullPath(solutionPath);
			}
		} else {

			int threadnum = Runtime.getRuntime().availableProcessors();
			ExecutorService threadpool = Executors.newFixedThreadPool(threadnum);
			List<Callable<List<Direction>>> tasks = new LinkedList<Callable<List<Direction>>>();
			List<Direction> result = new LinkedList<Direction>();
			Choice choice;
			try {
				choice = firstChoice(maze.getStart());
				while (!choice.choices.isEmpty()) {
					tasks.add(new DFS(follow(choice.at, choice.choices.peek()), choice.choices.pop()));
				}

			} catch (SolutionFound e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < tasks.size(); i++) {
				try {
					result = threadpool.submit(tasks.get(i)).get();
					if (result == null)
						continue;
					else
						break;
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			threadpool.shutdown();
			return result;
		}
	}

	private class DFS implements Callable<List<Direction>> {
		Choice startPt;
		Direction firstDir;

		public DFS(Choice startPt, Direction firstDir) {
			this.startPt = startPt;
			this.firstDir = firstDir;
		}

		@Override
		public List<Direction> call() {
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice ch;

			try {
				choiceStack.push(this.startPt);
				while (!choiceStack.isEmpty()) {
					ch = choiceStack.peek();
					if (ch.isDeadend()) {
						// backtrack.
						choiceStack.pop();
						if (!choiceStack.isEmpty())
							choiceStack.peek().choices.pop();
						continue;
					}
					choiceStack.push(follow(ch.at, ch.choices.peek()));
				}
				// No solution found.
				return null;
			} catch (SolutionFound e) {
				Iterator<Choice> iter = choiceStack.iterator();
				LinkedList<Direction> solutionPath = new LinkedList<Direction>();
				while (iter.hasNext()) {
					ch = iter.next();
					solutionPath.push(ch.choices.peek());
				}
				solutionPath.push(this.firstDir);

				if (maze.display != null)
					maze.display.updateDisplay();

				return pathToFullPath(solutionPath);
			}
		}
	}

}
