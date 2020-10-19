/**
 * A class to evaluate a personalised recommender.
 */

package alg.cbp.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alg.cb.casebase.Movie;
import alg.cb.util.Matrix;
import alg.cbp.recommender.PRecommender;

public class PEvaluator {
	private double threshold; // Movies with ratings >= threshold are considered liked by users
	private Map<Integer,List<Movie>> recommendations; // Stores the recommendations made for each user
	private Matrix testRatings;  // Stores the test ratings for each user

	/**
	 * constructor - creates a new Evaluator object
	 * @param recommender - the recommender
	 * @param threshold - movies with ratings >= threshold are considered liked by users
	 * @param trainRatings - a matrix which stores user training ratings
	 * @param testRatings - a matrix which stores user test ratings
	 */
	public PEvaluator(PRecommender recommender, double threshold, Matrix trainRatings, Matrix testRatings) {
		this.threshold = threshold;
		this.testRatings = testRatings;
		recommendations = new HashMap<>();

		// Get the recommendations for each user
		
		// Create a hash set to hold the target movies for the current user. The 
		// target movies are those movies in the training set which the user has 
		// liked (movies with ratings >= threshold are considered liked).

		for (int userId: trainRatings.getRowIds()) {
			
			Set<Movie> targetMovies = new HashSet<>();
			
			for (Integer MovieID : trainRatings.getColIds(userId)) {
				double UserRating = trainRatings.getValue(userId, MovieID);
				
				if (UserRating >= threshold) {
//					targetMovies.add(MovieID);
					targetMovies.add(recommender.getCasebase().getMovie(MovieID));
//					Add movie to the HashSet.
					
				}
			}
			// Get the recommendations for the current user based on the target movies
			// and add the recommendations to the map.
			
			List<Movie> recs = recommender.getRecommendations(targetMovies);
			recommendations.put(userId, recs);
		}
	}
	

	/**
	 * @return the coverage which is given by the percentage of  
	 * users for which at least one recommendation can be made
	 */
	public double getCoverage() {
		// Implement this method
		
		int counter = 0;
		
		for (Integer i : recommendations.keySet()) {
			if (recommendations.get(i).size() > 0) {
				counter ++;
			}
		}
		
		return counter * 1.0 / recommendations.size();
	}
	

	/**
	 * @param k - the number of recommendations to consider
	 * @return the average precision, recall, and F1 over all users for which at least one
	 * recommendation can be made
	 */
	public double[] getPRF1(int k) {
		
		double runningTotalPrecision = 0;
		double runningTotalRecall = 0;
		double runningTotalF1 = 0;
		
		for (Integer userId : testRatings.getRowIds()) {
			
			ArrayList<Integer> likedMovies = new ArrayList<>();
			
			for (Integer movieId : testRatings.getColIds(userId)) {
				if (testRatings.getValue(userId, movieId) >= threshold) {
					likedMovies.add(movieId);
				}
			}
			
		ArrayList<Integer> recommendedListIds = new ArrayList<>();
		
		for (Movie m : recommendations.get(userId)) {
			recommendedListIds.add(m.getId());
		}
		
		int relevantMovieCount = 0;
		
		for (int i = 0; i < k; i++) {
			if (likedMovies.contains(recommendedListIds.get(i))) {
				relevantMovieCount ++;
			}
		}
		
//		double precisionResult = (relevantMovieCount * 1.0) / k;
//		double precisionResult = ((relevantMovieCount > 0) ? (relevantMovieCount * 1.0 / k) : 0);
//		double recallResult = ((relevantMovieCount > 0 && (likedMovies.size() > 0)) ? (relevantMovieCount * 1.0 / likedMovies.size()) : 0);
//		double F1Result	= ((relevantMovieCount > 0) ? ((2.0 * precisionResult * recallResult) / (precisionResult + recallResult)) : 0);
//		
		
		double precision;
		if (relevantMovieCount > 0) {
			precision = (relevantMovieCount * 1.0) / (k * 1.0);
		} else {
			precision = 0;
		}
		
		double recall;
		if ((likedMovies.size() > 0) && (relevantMovieCount > 0)) {
			recall = (relevantMovieCount * 1.0) / (likedMovies.size() * 1.0);
		} else {
			recall = 0;
		}
		
		double F1Score;
		if ((precision > 0) && (recall > 0)) {
			F1Score = ((2.0 * precision * recall) / (precision + recall));
		} else {
			F1Score = 0;
		}
		
		runningTotalPrecision = runningTotalPrecision + precision;
		runningTotalRecall = runningTotalRecall + recall;
		runningTotalF1 = runningTotalF1 + F1Score;
			
		}
		
		
		
//		All movies in the map are relevant ie, over threshold.
		
		
		double[] values = new double[3];	
		values[0] = runningTotalPrecision / (testRatings.getRowIds().size() * 1.0);
		values[1] = runningTotalRecall / (testRatings.getRowIds().size() * 1.0);
		values[2] = runningTotalF1 / (testRatings.getRowIds().size() * 1.0);
		return values;
	}
}
