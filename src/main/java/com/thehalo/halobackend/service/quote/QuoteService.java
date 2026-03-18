package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.dto.quote.request.ReviewQuoteRequest;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;

import java.util.List;

public interface QuoteService {
    List<QuoteSummaryResponse> getMyQuotes();

    QuoteDetailResponse getDetail(Long quoteId);

    QuoteDetailResponse submit(SubmitQuoteRequest request);
    
    QuoteDetailResponse acceptQuote(Long quoteId);

    QuoteDetailResponse updateStatus(Long quoteId, com.thehalo.halobackend.dto.quote.request.QuoteStatusUpdateRequest request);
}
