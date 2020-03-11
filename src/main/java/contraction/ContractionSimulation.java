package contraction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContractionSimulation {
	static long msgSpeed;

	public static void main(String[] args) {
		System.out.println(getRatio(20));

		// System.out.println(getRatio(2000000, 4000000));
	}

	public static double getRatio(long msgSpeedOverSpeedRate) {
		int iterCount = 20;
		double[] ratios = new double[iterCount];
		double sum = 0;
		for (int i = 0; i < iterCount; i++) {
			ratios[i] = getRatio(msgSpeedOverSpeedRate, msgSpeedOverSpeedRate * ((i + 1) * 15));
			sum += ratios[i];
			// System.out.println(ratios[i]);
		}
		return sum / iterCount;
	}

	static double getRatio(long msgSpeedOverSpeedRate, long desiredDistance) {
		Aknowledge aKnowledge = new Aknowledge();
		aKnowledge.desiredDistance = desiredDistance;
		aKnowledge.distanceToB = aKnowledge.desiredDistance;
		aKnowledge.speed = 1;

		msgSpeed = msgSpeedOverSpeedRate;

		State state = new State(0L, aKnowledge.desiredDistance);

		MessageTracker tracker = new MessageTracker();

		double totalDistance = 0;

		AMessage aMessage = new AMessage(state.getaPos(), aKnowledge, true);
		tracker.addAMessage(aMessage, aKnowledge, state);

		int iterCount = 2000;
		for (int i = 0; i < iterCount; i++) {
			state.changeAPos(aKnowledge.speed);
			aKnowledge.distanceToB -= aKnowledge.speed;

			tracker.process(aKnowledge, state);

			long d = state.getbPos() - state.getaPos();
			totalDistance += d;
			// System.out.println(state.getaPos() + ", " + state.getbPos() + ", d: " + d +
			// ", aK: " + aKnowledge.distanceToB);
		}

		double d = totalDistance / (double) iterCount;
		double ratio = d / aKnowledge.desiredDistance;
		return ratio;
	}

	static class Aknowledge {
		long distanceToB;
		long desiredDistance;
		long speed;
	}

	static class AMessage extends BaseMessage {
		long bDiff;
		long msgAge;
		boolean continuos;

		public AMessage(long startPos, Aknowledge ak, boolean continuous) {
			this.start = startPos;
			this.end = startPos;
			this.continuos = continuous;

			if (ak.desiredDistance < msgSpeed) {
				bDiff = ak.speed;
			} else {
				bDiff = -ak.distanceToB + ak.desiredDistance;
			}
		}
	}

	static class BMessage extends BaseMessage {
		long bPosChange;
		long msgAge;
		AMessage originalAMessage;

		public BMessage(long startPos, AMessage aMsg) {
			this.start = startPos;
			this.end = startPos;
			this.originalAMessage = aMsg;
		}
	}

	static class BaseMessage {
		long start, end;
	}

	static class MessageTracker {
		private List<AMessage> aList = new LinkedList<ContractionSimulation.AMessage>();
		private List<BMessage> bList = new LinkedList<ContractionSimulation.BMessage>();

		public void addAMessage(AMessage msg, Aknowledge ak, State state) {
			aList.add(msg);
		}

		public void process(Aknowledge ak, State state) {
			Iterator<AMessage> aIter = aList.iterator();
			for (; aIter.hasNext();) {
				AMessage aMessage = aIter.next();
				aMessage.start -= msgSpeed;
				aMessage.end += msgSpeed;
				aMessage.msgAge++;

				if (processAMessage(ak, aMessage, state)) {
					aIter.remove();
				}
			}

			Iterator<BMessage> bIter = bList.iterator();
			for (; bIter.hasNext();) {
				BMessage bMessage = bIter.next();
				bMessage.start -= msgSpeed;
				bMessage.end += msgSpeed;
				bMessage.msgAge++;

				if (processBMessage(ak, bMessage, state)) {
					bIter.remove();
				}
			}
		}

		boolean processAMessage(Aknowledge ak, AMessage aMessage, State state) {
			if (aMessage.start <= state.getbPos() && state.getbPos() <= aMessage.end) {
				state.changeBPos(aMessage.bDiff);

				BMessage msg = new BMessage(state.getbPos(), aMessage);
				msg.bPosChange = aMessage.bDiff;
				bList.add(msg);
				return true;
			}
			return false;
		}

		boolean processBMessage(Aknowledge ak, BMessage bMessage, State state) {
			if (bMessage.start <= state.getaPos() && state.getaPos() <= bMessage.end) {
				ak.distanceToB += bMessage.bPosChange;

				if (ak.distanceToB != ak.desiredDistance) {
					AMessage aMessage = new AMessage(state.getaPos(), ak, bMessage.originalAMessage.continuos);
					aList.add(aMessage);
				} else if (ak.desiredDistance < msgSpeed && bMessage.originalAMessage.continuos) {
					AMessage aMessage = new AMessage(state.getaPos(), ak, true);
					aList.add(aMessage);
				}

				return true;
			}
			return false;
		}
	}
}
