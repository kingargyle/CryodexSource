package cryodex.modules.imperialassault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class IARandomMatchGeneration {

	private final IATournament tournament;
	private final List<IAPlayer> players;

	public IARandomMatchGeneration(IATournament tournament,
			List<IAPlayer> players) {
		this.tournament = tournament;
		this.players = players;
	}

	public List<IAMatch> generateMatches() {

		TreeMap<Integer, List<IAPlayer>> playerMap = new TreeMap<Integer, List<IAPlayer>>(
				new Comparator<Integer>() {

					@Override
					public int compare(Integer arg0, Integer arg1) {
						return arg0.compareTo(arg1) * -1;
					}
				});

		for (IAPlayer xp : players) {
			Integer points = xp.getScore(tournament);

			List<IAPlayer> pointGroup = playerMap.get(points);

			if (pointGroup == null) {
				pointGroup = new ArrayList<>();
				playerMap.put(points, pointGroup);
			}

			pointGroup.add(xp);
		}

		Integer firstSet = playerMap.keySet().iterator().next();

		List<IAMatch> matches = resolvePointGroup(null, playerMap,
				playerMap.get(firstSet));

		return matches;
	}

	private List<IAMatch> resolvePointGroup(IAPlayer carryOverPlayer,
			TreeMap<Integer, List<IAPlayer>> playerMap,
			List<IAPlayer> playerList) {

		Collections.shuffle(playerList);

		IAPlayer newCarryOverPlayer = null;
		int carryOverPlayerIndex = playerList.size();
		boolean isCarryOver = carryOverPlayer == null ? carryOverPlayerIndex % 2 == 1
				: carryOverPlayerIndex % 2 == 0;

		while (true) {

			List<IAPlayer> tempList = new ArrayList<>();
			tempList.addAll(playerList);

			if (isCarryOver) {
				carryOverPlayerIndex--;
				newCarryOverPlayer = playerList.get(carryOverPlayerIndex);
				tempList.remove(newCarryOverPlayer);
			}

			List<IAMatch> returnedMatches = getRandomMatches(
					carryOverPlayer, tempList);

			// If the list was good or if there was no carry over players that
			// can change things up
			if (isCarryOver == false || carryOverPlayerIndex == 0
					|| IAMatch.hasDuplicate(returnedMatches) == false) {

				List<IAPlayer> nextPointGroup = null;

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
					List<IAMatch> nextPointGroupMatches = resolvePointGroup(
							newCarryOverPlayer, playerMap, nextPointGroup);

					// Again, continue if the list is good or there are no other
					// options
					if (isCarryOver == false
							|| carryOverPlayerIndex == 0
							|| IAMatch.hasDuplicate(nextPointGroupMatches) == false) {
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
	private List<IAMatch> getRandomMatches(IAPlayer carryOverPlayer,
			List<IAPlayer> players) {

		List<IAMatch> matches = new ArrayList<>();

		// if there are no players, return no matches
		if (players.isEmpty()) {
			return matches;
		}

		IAMatch m = new IAMatch();

		List<IAMatch> subMatches = new ArrayList<>();

		// If there is a carry over player, they are always player 1
		if (carryOverPlayer != null) {
			m.setPlayer1(carryOverPlayer);
			for (int counter = 0; counter < players.size(); counter++) {

				m.setPlayer2(players.get(counter));
				m.checkDuplicate(tournament.getAllRounds());

				// Continue if the match is not a duplicate or this is the last
				// chance
				if (m.isDuplicate() == false || counter == players.size() - 1) {
					List<IAPlayer> nextPlayers = new ArrayList<IAPlayer>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer2());
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (IAMatch.hasDuplicate(subMatches) == false) {
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
					List<IAPlayer> nextPlayers = new ArrayList<IAPlayer>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer1());
					nextPlayers.remove(m.getPlayer2());

					// Call function recursively
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (IAMatch.hasDuplicate(subMatches) == false) {
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
