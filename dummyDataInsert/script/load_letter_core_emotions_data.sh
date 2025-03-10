#!/bin/bash

DB_USER=$1
DB_PASSWORD=$2
DB_NAME="bside"
CSV_DIR="/var/lib/mysql-files/csv/letter_core_emotions"

# 병렬 처리를 위한 최대 동시 실행 프로세스 개수
MAX_JOBS=$3
JOBS=0

# 데이터 로드 작업
for FILE in "$CSV_DIR"/letter_core_emotions_*.csv; do
  echo "Processing $FILE..."

  # LOAD DATA 실행 (백그라운드로)
  (
    mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
      START TRANSACTION;
      LOAD DATA INFILE '$FILE'
      INTO TABLE letter_core_emotions
      CHARACTER SET utf8mb4
      FIELDS TERMINATED BY ','
      ENCLOSED BY '\"'
      LINES TERMINATED BY '\r\n'
      IGNORE 1 LINES
      (letter_analysis_id, core_emotion);
      COMMIT;
    "
    echo "Finished processing $FILE"
  ) &

  # 병렬 작업 수 증가
  JOBS=$((JOBS + 1))

  # 최대 동시 실행 개수에 도달하면 작업 완료 대기
  if [ "$JOBS" -ge "$MAX_JOBS" ]; then
    wait
    JOBS=0
  fi
done

# 남은 백그라운드 작업 대기
wait

echo "✅ 모든 일일분석별 대표 감정 데이터를 로드 완료!"
