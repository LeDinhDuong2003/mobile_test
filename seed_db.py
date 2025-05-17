from models import User, Course, Review, Base
from database import SessionLocal, engine
from datetime import datetime

def seed_database():
    # Tạo tất cả các bảng nếu chưa tồn tại
    Base.metadata.create_all(bind=engine)
    
    db = SessionLocal()
    
    try:
        # Kiểm tra xem đã có dữ liệu chưa
        if db.query(User).count() > 0:
            print("Dữ liệu đã tồn tại, bỏ qua quá trình seed!")
            return
        
        # Tạo người dùng mẫu
        user1 = User(
            username="nguyenvana",
            full_name="Nguyễn Văn A",
            password="password123",
            email="nguyenvana@example.com",
            phone="0123456789",
            avatar_url="https://randomuser.me/api/portraits/men/1.jpg",
            role="instructor"
        )
        
        user2 = User(
            username="tranthib",
            full_name="Trần Thị B",
            password="password123",
            email="tranthib@example.com", 
            phone="0987654321",
            avatar_url="https://randomuser.me/api/portraits/women/1.jpg",
            role="instructor"
        )
        
        db.add(user1)
        db.add(user2)
        db.commit()
        
        # Tạo khóa học mẫu
        courses = [
            Course(
                owner_id=user1.user_id,
                title="Lập trình Android từ cơ bản đến nâng cao",
                description="Khóa học giúp bạn học lập trình Android từ cơ bản đến nâng cao một cách nhanh chóng",
                thumbnail_url="https://play-lh.googleusercontent.com/PCpXdqvUWfCW1mXhH1Y_98yBpgsWxuTSTofy3NGMo9yBTATDyzVkqU580bfSln50bFU",
                price=299000,
                created_at=datetime.utcnow()
            ),
            Course(
                owner_id=user2.user_id,
                title="Thiết kế UI/UX hiện đại",
                description="Học cách thiết kế giao diện người dùng đẹp mắt và trải nghiệm người dùng tốt nhất",
                thumbnail_url="https://img-c.udemycdn.com/course/750x422/2489934_ebe8_5.jpg",
                price=399000,
                created_at=datetime.utcnow()
            ),
            Course(
                owner_id=user1.user_id,
                title="Lập trình ReactJS từ zero",
                description="Học ReactJS từ đầu đến cuối, xây dựng các ứng dụng web hiện đại",
                thumbnail_url="https://img-c.udemycdn.com/course/750x422/1362070_b9a1_2.jpg",
                price=199000,
                created_at=datetime.utcnow()
            ),
            Course(
                owner_id=user2.user_id,
                title="Python cho người mới bắt đầu",
                description="Học lập trình Python từ số 0 đến có thể làm việc",
                thumbnail_url="https://img-c.udemycdn.com/course/750x422/394676_ce3d_5.jpg",
                price=249000,
                created_at=datetime.utcnow()
            ),
        ]
        
        for course in courses:
            db.add(course)
        db.commit()
        
        # Tạo đánh giá mẫu
        for course in courses:
            for i in range(5):
                review = Review(
                    user_id=user1.user_id if i % 2 == 0 else user2.user_id,
                    course_id=course.course_id,
                    rating=4 + (i % 2),  # Rating từ 4-5
                    comment=f"Đánh giá số {i+1} cho khóa học này. Rất hay và bổ ích!",
                    created_at=datetime.utcnow()
                )
                db.add(review)
        db.commit()
        
        print("Đã thêm dữ liệu mẫu thành công!")
        
    except Exception as e:
        db.rollback()
        print(f"Lỗi: {str(e)}")
    finally:
        db.close()

if __name__ == "__main__":
    seed_database()