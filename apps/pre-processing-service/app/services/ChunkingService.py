from app.config.logging.ServiceLogger import service_logger


@service_logger.chunking_class()
class ChunkingService:

    def chunk_text(self, text: str, chunk_size: int = 100, overlap: int = 20):
        """텍스트를 청크로 분할"""
        chunks = []

        for i in range(0, len(text), chunk_size - overlap):
            chunk = text[i:i + chunk_size]
            if chunk.strip():
                chunks.append(chunk.strip())

        return chunks