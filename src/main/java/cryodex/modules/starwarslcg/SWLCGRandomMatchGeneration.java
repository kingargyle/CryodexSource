package cryodex.modules.starwarslcg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class SWLCGRandomMatchGeneration {

	private final SWLCGTournament tournament;
	private final List<SWLCGPlayer> players;

	public SWLCGRandomMatchGeneration(SWLCGTournament tournament,
			List<SWLCGPlayer> players) {
		this.tournament = tournament;
		this.players = players;
	}

	public List<SWLCGMatch> generateMatches() {

		TreeMap<Integer, List<SWLCGPlayer>> playerMap = new TreeMap<Integer, List<SWLCGPlayer>>(
				new Comparator<Integer>() {

					@Override
					public int compare(Integer arg0, Integer arg1) {
						return arg0.compareTo(arg1) * -1;
					}
				});

		for (SWLCGPlayer xp : players) {
			Integer points = xp.getScore(tournament);

			List<SWLCGPlayer> pointGroup = playerMap.get(points);

			if (pointGroup == null) {
				pointGroup = new ArrayList<>();
				playerMap.put(points, pointGroup);
			}

			pointGroup.add(xp);
		}

		Integer firstSet = playerMap.keySet().iterator().next();

		List<SWLCGMatch> matches = resolvePointGroup(null, playerMap,
				playerMap.get(firstSet));

		return matches;
	}

	private List<SWLCGMatch> resolvePointGroup(SWLCGPlayer carryOverPlayer,
			TreeMap<Integer, List<SWLCGPlayer>> playerMap,
			List<SWLCGPlayer> playerList) {

		Collections.shuffle(playerList);

		SWLCGPlayer newCarryOverPlayer = null;
		int carryOverPlayerIndex = playerList.size();
		boolean isCarryOver = carryOverPlayer == null ? carryOverPlayerIndex % 2 == 1
				: carryOverPlayerIndex % 2 == 0;

		while (true) {

			List<SWLCGPlayer> tempList = new ArrayList<>();
			tempList.addAll(playerList);

			if (isCarryOver) {
				carryOverPlayerIndex--;
				newCarryOverPlayer = playerList.get(carryOverPlayerIndex);
				tempList.remove(newCarryOverPlayer);
			}

			List<SWLCGMatch> returnedMatches = getRandomMatches(
					carryOverPlayer, tempList);

			// If the list was good or if there was no carry over players that
			// can change things up
			if (isCarryOver == false || carryOverPlayerIndex == 0
					|| SWLCGMatch.hasDuplicate(returnedMatches) == false) {

				List<SWLCGPlayer> nextPointGroup = null;

				boolean next = false;
				for (Integer points : playerMap.keySet()) {

					if (next) {
						nextPointGroup = playerMap.get(points);
						break;
					}

					if (playerMap.get(points) == playerList) {
						next = true;
					}
				}

				// If this was the last point group
				if (nextPointGroup == null) {

					return returnedMatches;
				} else {
					// Else, check the next point group
					List<SWLCGMatch> nextPointGroupMatches = resolvePointGroup(
							newCarryOverPlayer, playerMap, nextPointGroup);

					// Again, continue if the list is good or there are no other
					// options
					if (isCarryOver == false
							|| carryOverPlayerIndex == 0
							|| SWLCGMatch.hasDuplicate(nextPointGroupMatches) == false) {
						returnedMatches.addAll(nextPointGroupMatches);
						return returnedMatches;
					}
				}
			}
		}
	}

	/**
	 * Recursive call to find a non duplicate match of remaining players in a
	 * group
	 * 
	 * @param carryOverPlayer
	 * @param players
	 * @return
	 */
	private List<SWLCGMatch> getRandomMatches(SWLCGPlayer carryOverPlayer,
			List<SWLCGPlayer> players) {

		List<SWLCGMatch> matches = new ArrayList<>();

		// if there are no players, return no matches
		if (players.isEmpty()) {
			return matches;
		}

		SWLCGMatch m = new SWLCGMatch();

		List<SWLCGMatch> subMatches = new ArrayList<>();

		// If there is a carry over player, they are always player 1
		if (carryOverPlayer != null) {
			m.setPlayer1(carryOverPlayer);
			for (int counter = 0; counter < players.size(); counter++) {

				m.setPlayer2(players.get(counter));
				m.checkDuplicate(tournament.getAllRounds());

				// Continue if the match is not a duplicate or this is the last
				// chance
				if (m.isDuplicate() == false || counter == players.size() - 1) {
					List<SWLCGPlayer> nextPlayers = new ArrayList<SWLCGPlayer>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer2());
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (SWLCGMatch.hasDuplicate(subMatches) == false) {
						matches.add(m);
						matches.addAll(subMatches);
						return matches;
					}
				}
			}
		} else {

			// this is the same as the previous one, except the first player in
			// the list is player 1 and both player 1 and 2 must be removed from
			// the continuing list

			m.setPlayer1(players.get(0));

			// Loop through each other player to find a match
			for (int counter = 1; counter < players.size(); counter++) {

				// add new player and check if it is valid
				m.setPlayer2(players.get(counter));
				m.checkDuplicate(tournament.getAllRounds());

				// Continue if the match is not a duplicate or this is the last
				// chance
				if (m.isDuplicate() == false || counter == players.size() - 1) {

					// Create the list of remaining players
					List<SWLCGPlayer> nextPlayers = new ArrayList<SWLCGPlayer>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer1());
					nextPlayers.remove(m.getPlayer2());

					// Call function recursively
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (SWLCGMatch.hasDuplicate(subMatches) == false) {
						matches.add(m);
						matches.addAll(subMatches);
						return matches;
					}
				}
			}

		}

		// If we got here than we gave up, there was no chance of finding a non
		// duplicate group
		matches.add(m);
		matches.addAll(subMatches);
		return matches;
	}
}
