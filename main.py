from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.sql import func
from database import SessionLocal, get_db
from models import User, Course, Review
from pydantic import BaseModel
from datetime import datetime
from typing import List
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

app = FastAPI()

# Cho phép gọi từ Android
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Model nhận từ Android
class LoginRequest(BaseModel):
    username: str
    password: str

class GoogleLoginRequest(BaseModel):
    id_token: str
    email: str
    display_name: str | None
    photo_url: str | None

# Model trả về cho Android
class UserResponse(BaseModel):
    user_id: int
    full_name: str
    email: str
    phone: str | None
    avatar_url: str | None
    google_id: str | None
    role: str | None

    class Config:
        from_attributes = True

# Model phản hồi cho khóa học
class CourseResponse(BaseModel):
    course_id: int
    title: str
    description: str | None
    thumbnail_url: str
    price: float
    rating: float | None
    instructor_name: str
    is_bestseller: bool = False
    
    class Config:
        from_attributes = True

@app.post("/auth/login", response_model=UserResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.username == request.username).first()
    if not user or user.password != request.password:
        raise HTTPException(status_code=401, detail="Sai username hoặc password")
    return user

@app.post("/auth/google", response_model=UserResponse)
def google_login(request: GoogleLoginRequest, db: Session = Depends(get_db)):
    # Kiểm tra xem google_id (id_token) đã tồn tại chưa
    user = db.query(User).filter(User.google_id == request.id_token).first()
    
    if user:
        # Nếu đã có, trả về user
        return user
    
    # Nếu chưa, tạo user mới
    user = User(
        username=request.email.split("@")[0],  # Tạo username từ email
        email=request.email,
        google_id=request.id_token,
        full_name=request.display_name or "Google User",
        avatar_url=request.photo_url,
        role="user",
        password=None  # Không cần password cho Google login
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    
    return user

# Danh sách khóa học nổi bật
@app.get("/api/courses/top", response_model=List[CourseResponse])
def get_top_courses(db: Session = Depends(get_db)):
    courses = db.query(Course).join(User, Course.owner_id == User.user_id).limit(10).all()
    
    # Tính rating trung bình cho mỗi khóa học từ bảng reviews
    result = []
    for course in courses:
        # Lấy rating trung bình
        avg_rating = db.query(func.avg(Review.rating)).filter(
            Review.course_id == course.course_id).scalar() or 4.5
        
        # Kiểm tra xem có phải bestseller không (có ít nhất 3 đánh giá và rating >= 4.5)
        is_bestseller = False
        reviews_count = db.query(func.count(Review.review_id)).filter(
            Review.course_id == course.course_id).scalar() or 0
        if reviews_count >= 3 and avg_rating >= 4.5:
            is_bestseller = True
            
        result.append({
            "course_id": course.course_id,
            "title": course.title,
            "description": course.description,
            "thumbnail_url": course.thumbnail_url,
            "price": course.price or 0.0,
            "rating": round(avg_rating, 1),
            "instructor_name": course.instructor.full_name,
            "is_bestseller": is_bestseller
        })
    
    return result

# Lấy chi tiết một khóa học
@app.get("/api/courses/{course_id}", response_model=CourseResponse)
def get_course_detail(course_id: int, db: Session = Depends(get_db)):
    course = db.query(Course).filter(Course.course_id == course_id).first()
    if not course:
        raise HTTPException(status_code=404, detail="Khóa học không tồn tại")
    
    # Lấy rating trung bình
    avg_rating = db.query(func.avg(Review.rating)).filter(
        Review.course_id == course.course_id).scalar() or 4.5
    
    # Kiểm tra xem có phải bestseller không
    is_bestseller = False
    reviews_count = db.query(func.count(Review.review_id)).filter(
        Review.course_id == course.course_id).scalar() or 0
    if reviews_count >= 3 and avg_rating >= 4.5:
        is_bestseller = True
    
    return {
        "course_id": course.course_id,
        "title": course.title,
        "description": course.description,
        "thumbnail_url": course.thumbnail_url,
        "price": course.price or 0.0,
        "rating": round(avg_rating, 1),
        "instructor_name": course.instructor.full_name,
        "is_bestseller": is_bestseller
    }

# Lấy banner cho trang chủ
@app.get("/api/banners")
def get_banner():
    return {
        "image_url": "https://img-c.udemycdn.com/notices/web_banner/image_udlite/b8f18e5c-c5c0-43e3-803e-4a1b89054543.jpg",
        "title": "Học hỏi không giới hạn!",
        "subtitle": "Khám phá hàng ngàn khóa học chất lượng cao"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)