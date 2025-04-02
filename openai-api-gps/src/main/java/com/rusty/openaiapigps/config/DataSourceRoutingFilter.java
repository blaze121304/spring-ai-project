package com.rusty.openaiapigps.config;

import com.rusty.openaiapigps.config.datasource.DataSourceContextHolder;
import com.rusty.openaiapigps.config.datasource.LookupKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.NoSuchElementException;


@Component
@Slf4j
@RequiredArgsConstructor
public class DataSourceRoutingFilter extends OncePerRequestFilter {

    private final DataSourceManager dataSourceManager;
    private final DataSourceMapRepository dataSourceMapRepository;

    /**
     * 요청에 대해 필터링 수행
     * <p>
     * 헤더에서 라우팅 키를 추출하고, 해당하는 데이터 소스를 설정합니다.
     *
     * @param request  요청 객체
     * @param response 응답 객체
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            LookupKey lookupKey = getRoutingKeyFromHeader(request);
            dataSourceManager.setCurrent(lookupKey);
            DataSourceContextHolder.setRoutingKey(lookupKey);
            filterChain.doFilter(request, response);
        } catch (NoSuchElementException e) {
            log.error("DataSource 정보를 찾을 수 없습니다: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid company code");
        } catch (Exception e) {
            log.error("DataSource 라우팅 중 오류 발생: " + e.getMessage(), e);
            throw e;
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 요청 헤더에서 회사 코드를 추출하고 해당하는 데이터 소스를 찾아 LookupKey 생성
     *
     * @param request 요청 객체
     * @return 생성된 LookupKey
     * @throws IllegalArgumentException 회사 코드가 없거나 유효하지 않을 경우
     * @throws NoSuchElementException 찾을 수 없는 회사 코드일 경우
     */
    private LookupKey getRoutingKeyFromHeader(HttpServletRequest request) {
        String companyCode = request.getHeader("companyCode");
        if (companyCode == null || companyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("회사 코드가 비어 있습니다.");
        }

        DataSourceMap dataSourceMap = dataSourceMapRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않은 DataSource 정보입니다: " + companyCode));

        return LookupKey.builder()
                .url(dataSourceMap.getUrl())
                .userName(dataSourceMap.getUserName())
                .password(dataSourceMap.getPassword())
                .driver(dataSourceMap.getDriver())
                .build();
    }

}
