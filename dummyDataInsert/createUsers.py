import traceback

import pandas as pd
import numpy as np

from ElapsedTimer import ElapsedTimer
from common import generate_unique_uuids

class CreateUsers:
    def __init__(self, user_filename, user_cnt, start_date, end_date, time_frequency):
        self.user_filename = user_filename
        self.user_cnt = user_cnt
        self.start_date = start_date
        self.end_date = end_date
        self.time_frequency = time_frequency
        self.timer = ElapsedTimer('users 생성')

    def create_users(self):
        try:
            self.timer.start()
            # dormant_at, isDormant의 경우 휴면계정 관련 내용이므로 nullable, 추가하지 않았음
            # profile_image_url도 마찬가지
            users = {
                'user_id': list(generate_unique_uuids(self.user_cnt)),
                'created_at': pd.to_datetime(
                    np.random.choice(pd.date_range(self.start_date, self.end_date, freq=self.time_frequency), size=self.user_cnt)
                ),
                'agree_to_privacy_policy': [b'1' for _ in range(self.user_cnt)],
                'agree_to_terms': [b'1' for _ in range(self.user_cnt)],
                'email': [f'user{i}@email.none' for i in range(self.user_cnt)],
                'is_email_ads_consented': [b'0' for _ in range(self.user_cnt)],
                'is_synced': [b'0' for _ in range(self.user_cnt)],
                'nickname': [f'user{i}' for i in range(self.user_cnt)],
                'preference': [np.random.choice(['T', 'F']) for _ in range(self.user_cnt)],
                'oauth2_provider': ['KAKAO' for _ in range(self.user_cnt)],
                'role': ['OAUTH' for _ in range(self.user_cnt)],
                'username': [f'{i}@KAKAO' for i in range(self.user_cnt)]
            }

            users_df = pd.DataFrame(users)

            # CSV 파일에 데이터 쓰기
            users_df.to_csv(self.user_filename, index=False, encoding='utf-8')
            print(f"\n{self.user_filename} 파일이 생성되었습니다.")
        except Exception as e:
            print(f"\n에러 발생: {e}")
            traceback.print_exc()
        finally:
            self.timer.stop()
            print('\nuser 생성 프로그램 종료')