package project.model;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.controller.FitnessBot;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import javax.xml.transform.Result;

@Service
public final class TrainingLibrary {
	@Getter
	private HashMap<Integer, Exercise> exerciseMap = new HashMap<>();


	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final Logger log = Logger.getLogger(FitnessBot.class);

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

			// Запрос для упражнений с пользователя
			String customQuery = "SELECT id, name, description, sets, repetitions FROM customExercise";
			executeQuery(connection, customQuery, (id, params) ->
					exerciseMap.put(id, new CustomExercise(
							(String) params[1],
							(String) params[2],
							(Integer) params[3],
							(Integer) params[4]
					))
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		String url = "jdbc:postgresql://localhost:5432/template1";
		String username = "postgres";
		String password = "1111";
		Set<Integer> keySet = new HashSet<>();

		try {
			Connection connection = DriverManager.getConnection(url, username, password);

			String customQuery = "SELECT id, name, description, sets, repetitions FROM customExercise";
			PreparedStatement preparedStatement = connection.prepareStatement(customQuery);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Object[] params = null;
				params = new Object[]{
						resultSet.getInt("id"),
						resultSet.getString("name"), // dont even ask
						resultSet.getString("description"),
						resultSet.getInt("sets"),
						resultSet.getInt("repetitions")
				};
				keySet.add((Integer) params[0]);
				exerciseMap.putIfAbsent((Integer) params[0], new CustomExercise(
								(String) params[1],
								(String) params[2],
								(Integer) params[3],
								(Integer) params[4]
						)
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		log.debug(keySet.toString());
		HashMap<Integer, Exercise> exercises = new HashMap<>();
		for (Integer entry : exerciseMap.keySet()) {
			if (entry > 1000) {
				exercises.put(entry, exerciseMap.get(entry));
			} else if (keySet.contains(entry)) {
				exercises.put(entry, exerciseMap.get(entry));
			}
		}
		exerciseMap = exercises;
	}

	private void executeQuery(Connection connection, String query, BiConsumer<Integer, Object[]> consumer) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		ResultSet resultSet = preparedStatement.executeQuery();

		while (resultSet.next()) {
			Object[] params = null;
			switch (query) {

				// customExercise в отличии от остальных таблиц вместо полей text[] имеет поля varchar
				// Для удобства CRUD
				case "SELECT id, name, description, sets, repetitions FROM customExercise":
					params = new Object[]{
							resultSet.getInt("id"),
							resultSet.getString("name"), // dont even ask
							resultSet.getString("description"),
							resultSet.getInt("sets"),
							resultSet.getInt("repetitions")
					};
					break;
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
