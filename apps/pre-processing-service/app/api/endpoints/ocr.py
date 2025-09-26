# app/api/endpoints/ocr.py
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from loguru import logger

from app.service.ocr.ChineseOCRTranlator import ChineseOCRTranslator
from app.service.ocr.S3Service import S3Service

router = APIRouter()


# Request/Response 모델들
class OCRProcessRequest(BaseModel):
    keyword: str


class OCRResult(BaseModel):
    s3_key: str
    chinese_text: str
    korean_text: str
    success: bool
    error: Optional[str] = None


class OCRProcessResponse(BaseModel):
    keyword: str
    total_objects: int
    jpg_files_count: int
    results: List[OCRResult]


@router.post("/process", response_model=OCRProcessResponse)
async def process_ocr_batch(request: OCRProcessRequest):
    """
    키워드로 S3 폴더 조회 → JPG 파일 필터링 → 중국어 OCR → 한국어 번역 (원스톱 처리)
    """
    try:
        logger.info(f"OCR 배치 처리 시작 - 키워드: {request.keyword}")

        # S3 서비스 및 OCR 서비스 초기화
        s3_service = S3Service(request.keyword)
        ocr_service = ChineseOCRTranslator()

        # S3에서 모든 객체 가져오기
        all_objects = s3_service.get_folder_objects()
        logger.info(f"총 {len(all_objects)}개 객체 발견")

        # JPG 파일만 필터링
        jpg_files = s3_service.get_jpg_files(all_objects)
        logger.info(f"JPG 파일 {len(jpg_files)}개 필터링 완료")

        if not jpg_files:
            logger.warning(f"키워드 '{request.keyword}'에 해당하는 JPG 파일이 없습니다.")
            return OCRProcessResponse(
                keyword=request.keyword,
                total_objects=len(all_objects),
                jpg_files_count=0,
                results=[]
            )

        # 각 JPG 파일 OCR 처리
        results = []
        for jpg_file in jpg_files:
            try:
                logger.info(f"OCR 처리 중: {jpg_file}")

                # 이미지 데이터 가져오기
                image_data = s3_service.get_image_data(jpg_file)

                # OCR 처리
                result = ocr_service.process_image_from_bytes(image_data)
                result["s3_key"] = jpg_file

                results.append(OCRResult(**result))
                logger.info(f"OCR 처리 완료: {jpg_file}")

            except Exception as e:
                logger.error(f"OCR 처리 실패 ({jpg_file}): {e}")
                results.append(OCRResult(
                    s3_key=jpg_file,
                    chinese_text="",
                    korean_text="",
                    success=False,
                    error=str(e)
                ))

        logger.info(f"OCR 배치 처리 완료 - 총 {len(results)}개 파일 처리됨")

        return OCRProcessResponse(
            keyword=request.keyword,
            total_objects=len(all_objects),
            jpg_files_count=len(jpg_files),
            results=results
        )

    except Exception as e:
        logger.error(f"OCR 배치 처리 실패 (키워드: {request.keyword}): {e}")
        raise HTTPException(
            status_code=500,
            detail=f"OCR 처리 중 오류가 발생했습니다: {str(e)}"
        )