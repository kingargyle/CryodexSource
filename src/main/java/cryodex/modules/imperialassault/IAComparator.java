package cryodex.modules.imperialassault;

import cryodex.modules.TournamentComparator;

public class IAComparator extends TournamentComparator<IAPlayer> {

	public static enum CompareOptions {
		HEAD_TO_HEAD, MARGIN_OF_VICTORY, STRENGH_OF_SCHEDULE, AVERAGE_STRENGTH_OF_SCHEDULE, EXTENDED_STRENGTH_OF_SCHEDULE, SCORE, RANDOM;
	}

	public static final CompareOptions[] pairingCompare = { CompareOptions.SCORE };
	public static final CompareOptions[] rankingCompare = {
			CompareOptions.SCORE,
			CompareOptions.AVERAGE_STRENGTH_OF_SCHEDULE,
			CompareOptions.EXTENDED_STRENGTH_OF_SCHEDULE, CompareOptions.RANDOM };

	private final IATournament t;
	private final CompareOptions[] sortOrder;

	public IAComparator(IATournament t, CompareOptions[] sortOrder) {
		this.t = t;
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(IAPlayer o1, IAPlayer o2) {

		int result = 0;

		for (CompareOptions option : sortOrder) {
			if (result == 0) {
				result = compareOption(o1, o2, option);
			}
		}

		return result;
	}

	private int compareOption(IAPlayer o1, IAPlayer o2, CompareOptions option) {

		int result = 0;

		switch (option) {
		case SCORE:
			result = compareInt(o1.getScore(t), o2.getScore(t));
			break;
		case HEAD_TO_HEAD:
			if (o1.getName().equals(o2.getName())) {
				return 0;
			}
			result = o1.isHeadToHeadWinner(t) ? 1 : 0;
			if(result == 0){
				result = o2.isHeadToHeadWinner(t) ? -1 : 0;
			}
			break;
		case STRENGH_OF_SCHEDULE:
			// Not implemented
			break;
		case AVERAGE_STRENGTH_OF_SCHEDULE:
			result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
			break;
		case EXTENDED_STRENGTH_OF_SCHEDULE:
			result = compareDouble(o1.getExtendedStrengthOfSchedule(t),
					o2.getExtendedStrengthOfSchedule(t));
			break;
		case MARGIN_OF_VICTORY:
			// Not Implemented
			break;
		case RANDOM:
			String seedValue1 = o1.getSeedValue();
			String seedValue2 = o2.getSeedValue();

			try {
				Double d1 = Double.valueOf(seedValue1);
				Double d2 = Double.valueOf(seedValue2);

				result = d1.compareTo(d2);
			} catch (NumberFormatException e) {
				result = seedValue1.compareTo(seedValue2);
			}
			break;
		}

		return result;
	}
}