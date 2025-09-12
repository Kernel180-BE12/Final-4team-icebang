from ...errors.CustomException import *
from ...errors.BlogPostingException import *
from fastapi import APIRouter, HTTPException

from ...model.schemas import *
from app.service.blog.blog_post_service_factory import BlogPostServiceFactory

router = APIRouter()


@router.get("/", summary="블로그 API 상태 확인")
async def root():
    return {"message": "blog API"}


@router.post(
    "/rag/create",
    response_model=ResponseBlogCreate,
    summary="RAG 기반 블로그 콘텐츠 생성",
)
async def rag_create(request: RequestBlogCreate):
    """
    RAG 기반 블로그 콘텐츠 생성
    """
    return {"message": "blog API"}


@router.post(
    "/publish",
    response_model=ResponseBlogPublish,
    summary="블로그 콘텐츠 배포 (네이버/티스토리/블로거 지원)",
)
async def publish(request: RequestBlogPublish):
    """
    생성된 블로그 콘텐츠를 배포합니다.
    네이버 블로그, 티스토리 블로그, 블로거를 지원합니다.
    Factory 패턴을 사용하여 플랫폼별 서비스를 생성합니다.
    """
    try:
        # Factory를 통해 서비스 생성
        blog_service = BlogPostServiceFactory.create_service(request.tag)

        # 블로그 포스팅 실행
        result = blog_service.post_content(
            title=request.post_title,
            content=request.post_content,
            tags=request.post_tags,
        )

        return ResponseBlogPublish(
            job_id=1,
            schedule_id=1,
            schedule_his_id=1,
            status="200",
            metadata=result
        )

    except ValueError as e:
        # 지원하지 않는 플랫폼
        raise HTTPException(
            status_code=400,
            detail=f"지원하지 않는 플랫폼입니다: {request.tag}"
        )

    except BlogLoginException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)

    except BlogContentValidationException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)

    except BlogPostPublishException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)

    except BlogServiceUnavailableException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)

    except BlogServiceInitializationException as e:
        raise HTTPException(status_code=e.status_code, detail=e.detail)

    except Exception as e:
        # 예상치 못한 오류
        raise HTTPException(
            status_code=500,
            detail=f"블로그 포스팅 중 예상치 못한 오류가 발생했습니다: {str(e)}"
        )