package project.model;
import lombok.Getter;
import java.sql.*;
import java.util.HashMap;
import java.util.function.BiConsumer;

public final class TrainingLibrary {
	@Getter
	private final HashMap<Integer, Exercise> exerciseMap = new HashMap<>();

	public void initialize() {
		String url = "jdbc:postgresql://localhost:5432/template1";
		String username = "postgres";
		String password = "1111";

		try {
			Connection connection = DriverManager.getConnection(url, username, password);

			// Запрос для обычных упражнений
			String ordinaryQuery = "SELECT id, name, description, sets, repetitions FROM ordinaryExercise";
			executeQuery(connection, ordinaryQuery, (id, params) ->
					exerciseMap.put(id, new OrdinaryExercise(
							(String) params[1],
							(String) params[2],
							(Integer) params[3],
							(Integer) params[4]
					))
			);

			// Запрос для упражнений на время
			String timeQuery = "SELECT id, name, description, sets, timeInSeconds FROM timeExercise";
			executeQuery(connection, timeQuery, (id, params) ->
					exerciseMap.put(id, new TimeExercise(
							(String) params[1],
							(String) params[2],
							(Integer) params[3],
							(Float) params[4]
					))
			);

			// Запрос для упражнений с весом
			String weightQuery = "SELECT id, name, description, sets, repetitions, weightPerRep FROM weightExercise";
			executeQuery(connection, weightQuery, (id, params) ->
					exerciseMap.put(id, new WeightExercise(
							(String) params[1],
							(String) params[2],
							(Integer) params[3],
							(Integer) params[4],
							(Float) params[5]
					))
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void executeQuery(Connection connection, String query, BiConsumer<Integer, Object[]> consumer) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		ResultSet resultSet = preparedStatement.executeQuery();

		while (resultSet.next()) {
			Object[] params = null;
			switch (query) {
				case "SELECT id, name, description, sets, repetitions FROM ordinaryExercise":
					params = new Object[]{
							resultSet.getInt("id"),
							resultSet.getString("name").substring(1, resultSet.getString("name").length() - 1).replaceAll("\"", ""),
							resultSet.getString("description").substring(2, resultSet.getString("description").length() - 2).replaceAll("\"", ""),
							resultSet.getInt("sets"),
							resultSet.getInt("repetitions")
					};
					break;
				case "SELECT id, name, description, sets, timeInSeconds FROM timeExercise":
					params = new Object[]{
							resultSet.getInt("id"),
							resultSet.getString("name").substring(1, resultSet.getString("name").length() - 1).replaceAll("\"", ""),
							resultSet.getString("description").substring(2, resultSet.getString("description").length() - 2).replaceAll("\"", ""),
							resultSet.getInt("sets"),
							resultSet.getFloat("timeInSeconds")
					};
					break;
				case "SELECT id, name, description, sets, repetitions, weightPerRep FROM weightExercise":
					params = new Object[]{
							resultSet.getInt("id"),
							resultSet.getString("name").substring(1, resultSet.getString("name").length() - 1).replaceAll("\"", ""),
							resultSet.getString("description").substring(2, resultSet.getString("description").length() - 2).replaceAll("\"", ""),
							resultSet.getInt("sets"),
							resultSet.getInt("repetitions"),
							resultSet.getFloat("weightPerRep")
					};
					break;
			}
			if (params!= null) {
				consumer.accept(resultSet.getInt("id"), params);
			}
		}
	}
}
