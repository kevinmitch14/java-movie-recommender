package alg.cb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alg.cb.casebase.Casebase;
import alg.cb.casebase.Movie;
import alg.cb.reader.DatasetReader;
import alg.cb.recommender.Recommender;
import alg.cb.similarity.ConfidenceSimilarity;
import alg.cb.similarity.GenomeCosineSimilarity;
import alg.cb.similarity.GenreJaccardSimilarity;
import alg.cb.similarity.GenreOverlapSimilarity;
import alg.cb.similarity.RatingsCosineSimilarity;
import alg.cb.similarity.SimilarityMetric;
import alg.cb.util.Matrix;
import alg.cbp.recommender.MaxPRecommender;
import alg.cbp.recommender.MeanPRecommender;
import alg.cbp.recommender.PRecommender;

public class myTest2 {
	public static void main(String[] args) {	
		// Set the paths and filenames and read in the data
		String movieFile = "dataset" + File.separator + "movies-sample.txt";
		String genomeScoresFile = "dataset" + File.separator + "genome-scores-sample.txt";
		String trainRatingsFile = "dataset" + File.separator + "ratings.txt";
		String testRatingsFile = "dataset" + File.separator + "test.txt";
		DatasetReader reader = new DatasetReader(movieFile, genomeScoresFile, trainRatingsFile, testRatingsFile);
		Casebase cb = reader.getCasebase();
		Matrix trainRatings = reader.getTrainRatings(); // a matrix to store the training ratings for each user
		Matrix testRatings = reader.getTestRatings(); // a matrix to store the test ratings for each user
		double threshold = 4; // movies with ratings >= threshold are considered liked by users


		// Configure the recommender
		SimilarityMetric s = new ConfidenceSimilarity(threshold);
		PRecommender r = new MeanPRecommender(cb, s);
		HashMap<Integer, List<Movie>> recom = new HashMap<>();




		for (int userId: trainRatings.getRowIds()) {

			Set<Movie> targetMovies = new HashSet<>();

			for (Integer MovieID : trainRatings.getColIds(userId)) {
				double UserRating = trainRatings.getValue(userId, MovieID);

				if (UserRating >= threshold) {
					//					targetMovies.add(MovieID);
					targetMovies.add(r.getCasebase().getMovie(MovieID));
					//					Add movie to the HashSet.

				}
			}


			// Get the recommendations for the current user based on the target movies
			// and add the recommendations to the map.
			// *** write this code here ***

			List<Movie> recs = r.getRecommendations(targetMovies);
			recom.put(userId, recs);


		}
		
//		System.out.println(recom.keySet());
//		int userid = 40967;


		double runningTotalPrecision = 0;
		double runningTotalRecall = 0;
		double runningTotalF1 = 0;

		for (Integer userid : testRatings.getRowIds()) {
			

			ArrayList<Integer> likedMovies = new ArrayList<>(); 

			for (Integer movieId : testRatings.getColIds(userid)) {
//				System.out.println("Movie Id " +movieId + "   Rating: " + testRatings.getValue(userid, movieId));
	
				if (testRatings.getValue(userid, movieId) >= 4.0) {
					likedMovies.add(movieId);
				} 
			}			
			
			int k = 50;
			
			System.out.println("==========" + "\n");
			System.out.println(userid);

			System.out.println("Number of good movies " + likedMovies.size());
			System.out.println("K = " + k);


			ArrayList<Integer> recMovieIds = new ArrayList<>();
			for (Movie m : recom.get(userid)) {
				recMovieIds.add(m.getId());
			}
			
//			System.out.println("IDs of the recommended movies " + recMovieIds);
//			System.out.println("===========");
			
			int counter = 0;
			
			for (int i = 0; i < k; i++) {
//				System.out.println(recMovieIds.get(i));
				if (likedMovies.contains(recMovieIds.get(i))) {
					counter ++;
				}
			}
			System.out.println("===========\n");
			System.out.println("Liked movies in the top " + k + " recommendations = " + counter);
			
			double precision;
			if (counter > 0) {
				precision = (counter * 1.0) / (k * 1.0);
			} else {
				precision = 0.0;
			}
			
			
			double recall = (counter * 1.0) / likedMovies.size() * 1.0;
			double recallUpdate;
			
			
			if ((likedMovies.size() > 0) && (counter > 0)) {
				recallUpdate = (counter * 1.0) / (likedMovies.size() * 1.0);
			} else {
				recallUpdate = 0.0;
			}
			
			System.out.println("Old precision: " +precision * 1.0 / 1);
			System.out.println("Old recall: " + recall);
			System.out.println("Updated recall: " + recallUpdate);
			System.out.println();
			System.out.println();
			runningTotalPrecision = runningTotalPrecision + precision * 1.0;
			runningTotalRecall = runningTotalRecall + recallUpdate * 1.0;
			
		}
		
		System.out.println(runningTotalPrecision);
		System.out.println(runningTotalRecall);
		System.out.println(testRatings.getRowIds().size());
		
		System.out.println((runningTotalPrecision * 1.0 / 1106));
		System.out.println(runningTotalRecall * 1.0 / 1106);
			
			
			

		


		
	}
}