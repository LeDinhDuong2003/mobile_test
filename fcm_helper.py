import json
import requests
from typing import List, Dict, Any, Optional

class FCMHelper:
    """Lớp hỗ trợ gửi thông báo qua Firebase Cloud Messaging."""
    
    # Thay thế bằng Server Key của bạn từ Firebase Console
    FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"
    FCM_URL = "https://fcm.googleapis.com/fcm/send"
    
    @classmethod
    def send_notification(cls,
                         tokens: List[str], 
                         title: str, 
                         body: str, 
                         data: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        Gửi thông báo đến một hoặc nhiều token FCM.
        
        Args:
            tokens: Danh sách token FCM
            title: Tiêu đề thông báo
            body: Nội dung thông báo
            data: Dữ liệu bổ sung cho thông báo (optional)
            
        Returns:
            Dict: Phản hồi từ FCM
        """
        if not tokens:
            return {"success": False, "error": "No FCM tokens provided"}
        
        # Chuẩn bị payload
        payload = {
            "registration_ids": tokens if len(tokens) > 1 else None,
            "to": tokens[0] if len(tokens) == 1 else None,
            "notification": {
                "title": title,
                "body": body,
                "sound": "default"
            },
            "priority": "high"
        }
        
        # Thêm dữ liệu nếu có
        if data:
            payload["data"] = data
        
        # Chuẩn bị headers
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"key={cls.FCM_SERVER_KEY}"
        }
        
        try:
            response = requests.post(
                cls.FCM_URL,
                headers=headers,
                data=json.dumps(payload)
            )
            
            return response.json()
        except Exception as e:
            return {"success": False, "error": str(e)}

    @classmethod
    def send_notification_to_user(cls, 
                                 db, 
                                 user_id: int, 
                                 title: str, 
                                 body: str, 
                                 notification_id: int = None,
                                 type: str = None,
                                 image_url: str = None) -> Dict[str, Any]:
        """
        Gửi thông báo đến một người dùng cụ thể.
        
        Args:
            db: Database session
            user_id: ID của người dùng
            title: Tiêu đề thông báo
            body: Nội dung thông báo
            notification_id: ID của thông báo trong DB (optional)
            type: Loại thông báo (optional)
            image_url: URL của hình ảnh (optional)
            
        Returns:
            Dict: Phản hồi từ FCM
        """
        # Lấy danh sách FCM token của user
        tokens = db.query(FCMToken.token).filter(FCMToken.user_id == user_id).all()
        tokens = [token[0] for token in tokens]
        
        if not tokens:
            return {"success": False, "error": "User has no FCM tokens"}
        
        # Chuẩn bị dữ liệu
        data = {
            "title": title,
            "message": body
        }
        
        if notification_id:
            data["notification_id"] = str(notification_id)
        
        if type:
            data["type"] = type
            
        if image_url:
            data["image_url"] = image_url
        
        # Gửi thông báo
        return cls.send_notification(tokens, title, body, data)
