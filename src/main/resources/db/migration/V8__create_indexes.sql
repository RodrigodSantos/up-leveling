CREATE INDEX idx_habits_user
ON habits(user_id);

CREATE INDEX idx_habit_logs_user
ON habit_logs(user_id);

CREATE INDEX idx_xp_transactions_user
ON xp_transactions(user_id);