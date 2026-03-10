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

    List<QuoteSummaryResponse> getUnderwriterQueue();

    QuoteDetailResponse reviewQuote(Long quoteId, ReviewQuoteRequest request, boolean approve);
}
