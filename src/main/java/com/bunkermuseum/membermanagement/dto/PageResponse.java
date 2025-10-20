package com.bunkermuseum.membermanagement.dto;

import java.util.List;

/**
 * Generic Data Transfer Object for paginated API responses.
 *
 * <p>This immutable DTO provides a standardized, reusable structure for returning
 * paginated data from REST endpoints. It wraps the actual data items along with
 * comprehensive pagination metadata, enabling efficient client-side pagination
 * controls and navigation.</p>
 *
 * <h3>Pagination Model</h3>
 * <p>This class follows the <strong>zero-indexed pagination model</strong> where:</p>
 * <ul>
 *   <li>First page is page 0</li>
 *   <li>Second page is page 1</li>
 *   <li>And so on...</li>
 * </ul>
 *
 *
 * <h3>Field Descriptions</h3>
 * <ul>
 *   <li><strong>content:</strong> The actual data items for the current page (never null, but may be empty)</li>
 *   <li><strong>page:</strong> Zero-indexed current page number (0 = first page)</li>
 *   <li><strong>size:</strong> Maximum number of items per page (requested page size)</li>
 *   <li><strong>totalElements:</strong> Total number of items across ALL pages</li>
 *   <li><strong>totalPages:</strong> Total number of pages needed to display all items</li>
 *   <li><strong>first:</strong> Boolean flag indicating if this is the first page (page == 0)</li>
 *   <li><strong>last:</strong> Boolean flag indicating if this is the last page</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>This class is <strong>thread-safe</strong> and <strong>immutable</strong>. Once constructed,
 * the instance cannot be modified. The content list is stored as-is, so callers should
 * ensure the list passed to the constructor is not modified externally.</p>
 *
 * <h3>Validation</h3>
 * <p>The constructor performs comprehensive validation:</p>
 * <ul>
 *   <li>content must not be null (empty list is allowed)</li>
 *   <li>page must be >= 0</li>
 *   <li>size must be > 0</li>
 *   <li>totalElements must be >= 0</li>
 * </ul>
 *
 * @param <T> The type of data being paginated (e.g., UserDTO, ProductDTO, etc.)
 *
 * @see org.springframework.data.domain.Page
 * @see org.springframework.data.domain.Pageable
 */
public class PageResponse<T> {

    /**
     * The data items for the current page.
     *
     * <p>This list contains the actual data elements returned for the current page.
     * The list is never null but may be empty if no results match the query.
     * The maximum size of this list is determined by the {@link #size} field,
     * but it may contain fewer items on the last page or when fewer results exist.</p>
     *
     * <p><strong>Example:</strong> If requesting page 2 with size 10, this list
     * contains items 20-29 (assuming enough items exist).</p>
     */
    private final List<T> content;

    /**
     * The current page number (0-indexed).
     *
     * <p>Pages are zero-indexed, meaning:</p>
     * <ul>
     *   <li>0 = first page</li>
     *   <li>1 = second page</li>
     *   <li>2 = third page</li>
     *   <li>etc.</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Frontend applications often display pages as 1-indexed
     * (showing "Page 1 of 5" instead of "Page 0 of 5"). Add 1 to this value for
     * user-facing displays.</p>
     *
     * <p><strong>Range:</strong> 0 to {@code totalPages - 1}</p>
     */
    private final int page;

    /**
     * The number of items per page (requested page size).
     *
     * <p>This represents the <strong>maximum</strong> number of items that can be
     * returned per page. The actual {@link #content} list may contain fewer items
     * in the following scenarios:</p>
     * <ul>
     *   <li>Last page with fewer remaining items (e.g., 3 items when size is 10)</li>
     *   <li>Total items in dataset is less than page size</li>
     *   <li>Search/filter results in fewer matches than page size</li>
     * </ul>
     *
     * <p><strong>Example:</strong> If size=10 and totalElements=25, pages 0 and 1
     * will have 10 items each, but page 2 will only have 5 items.</p>
     *
     * <p><strong>Validation:</strong> Must be greater than 0.</p>
     */
    private final int size;

    /**
     * The total number of items across ALL pages.
     *
     * <p>This is the total count of items in the entire dataset (not just the current page),
     * considering any active filters or search queries. This value is essential for:</p>
     * <ul>
     *   <li>Calculating the total number of pages ({@link #totalPages})</li>
     *   <li>Displaying "Showing X-Y of Z results" messages</li>
     *   <li>Determining if more pages exist</li>
     *   <li>Rendering pagination controls</li>
     * </ul>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>100 total users in database → totalElements = 100</li>
     *   <li>25 users match search query → totalElements = 25</li>
     *   <li>0 users match search query → totalElements = 0</li>
     * </ul>
     *
     * <p><strong>Type:</strong> long (not int) to support very large datasets (billions of records).</p>
     */
    private final long totalElements;

    /**
     * The total number of pages needed to display all items.
     *
     * <p>This is calculated as {@code Math.ceil(totalElements / size)}.
     * It represents how many pages are needed to display all available items
     * given the current page size.</p>
     *
     * <p><strong>Edge Case:</strong> When size=0, totalPages is set to 0 to avoid division by zero.</p>
     */
    private final int totalPages;

    /**
     * Boolean flag indicating whether this is the first page.
     *
     * <p>This flag is {@code true} when {@code page == 0}, {@code false} otherwise.</p>
     */
    private final boolean first;

    /**
     * Boolean flag indicating whether this is the last page.
     *
     * <p>This flag is {@code true} when:</p>
     * <ul>
     *   <li>{@code page >= totalPages - 1}, OR</li>
     *   <li>{@code totalPages <= 1} (single page or no pages)</li>
     * </ul>
     */
    private final boolean last;

    /**
     * Constructs a new PageResponse with comprehensive validation and automatic metadata calculation.
     *
     * <p>This constructor validates all input parameters and automatically calculates
     * derived fields ({@code totalPages}, {@code first}, {@code last}) based on the
     * provided values. All validations are performed before object construction to
     * maintain class invariants.</p>
     *
     * <h3>Validation Rules</h3>
     * <ul>
     *   <li><strong>content:</strong> Must not be null (empty list [] is valid)</li>
     *   <li><strong>page:</strong> Must be >= 0 (zero-indexed)</li>
     *   <li><strong>size:</strong> Must be > 0 (at least 1 item per page)</li>
     *   <li><strong>totalElements:</strong> Must be >= 0 (zero indicates no results)</li>
     * </ul>
     *
     * @param content The data items for the current page. Must not be null, but can be empty.
     *                The list should contain between 0 and {@code size} items.
     * @param page The current page number (0-indexed). Must be >= 0.
     *             First page is 0, second page is 1, etc.
     * @param size The number of items per page (page size). Must be > 0.
     *             Represents the maximum number of items per page, not the actual content size.
     * @param totalElements The total number of items across all pages. Must be >= 0.
     *                      This is the count of ALL items in the dataset, not just the current page.
     *
     * @throws IllegalArgumentException if {@code content} is null
     * @throws IllegalArgumentException if {@code page} is negative
     * @throws IllegalArgumentException if {@code size} is less than or equal to 0
     * @throws IllegalArgumentException if {@code totalElements} is negative
     *
     * @author Philipp Borkovic
     */
    public PageResponse(List<T> content, int page, int size, long totalElements) {
        if (content == null) {
            throw new IllegalArgumentException("Content list must not be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0, received: " + page);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0, received: " + size);
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must be >= 0, received: " + totalElements);
        }

        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;

        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        this.first = page == 0;

        this.last = totalPages <= 1 || page >= totalPages - 1;
    }

    /**
     * Gets the data items for the current page.
     *
     * <p>Returns the list of items contained in this page response. This list
     * is never null but may be empty if no results were found.</p>
     *
     * @return Non-null list of items for this page. May be empty but never null.
     *         The size of this list is between 0 and {@link #getSize()}.
     *
     * @author Philipp Borkovic
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Gets the current page number (0-indexed).
     *
     * <p>Returns the zero-based index of the current page. Remember to add 1
     * when displaying page numbers to users (UI convention).</p>
     *
     * @return The page number (0-indexed). Range: 0 to {@code totalPages - 1}.
     *         Returns 0 for the first page.
     *
     * @author Philipp Borkovic
     */
    public int getPage() {
        return page;
    }

    /**
     * Gets the number of items per page (requested page size).
     *
     * <p>Returns the maximum number of items that can appear on a single page.
     * Note that {@link #getContent()} may contain fewer items than this value,
     * especially on the last page.</p>
     *
     * <p><strong>Note:</strong> This is the <em>requested</em> size, not the
     * <em>actual</em> number of items on this page. Use {@code getContent().size()}
     * to get the actual number of items on the current page.</p>
     *
     * @return The page size (maximum items per page). Always > 0.
     *
     * @author Philipp Borkovic
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the total number of items across all pages.
     *
     * <p>Returns the total count of items in the entire dataset, considering
     * any active filters or search queries. This is NOT the size of the current
     * page content.</p>
     *
     * @return The total number of items across all pages. >= 0.
     *         Returns 0 if no items match the query/filter.
     *
     * @author Philipp Borkovic
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Gets the total number of pages needed to display all items.
     *
     * <p>Returns how many pages are required to display all {@link #getTotalElements()}
     * items given the current {@link #getSize()}. Calculated as
     * {@code Math.ceil(totalElements / size)}.</p>
     *
     * @return The total number of pages. >= 0.
     *         Valid page indices are 0 to {@code totalPages - 1}.
     *
     * @author Philipp Borkovic
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Checks if this is the first page.
     *
     * <p>Returns {@code true} if the current page is the first page (page 0),
     * {@code false} otherwise.</p>
     *
     * @return {@code true} if this is the first page (page == 0), {@code false} otherwise.
     *
     * @author Philipp Borkovic
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Checks if this is the last page.
     *
     * <p>Returns {@code true} if the current page is the last page or if there's
     * only one page total, {@code false} otherwise.</p>
     *
     * <p><strong>Note:</strong> If there are no results (totalElements=0),
     * both {@code isFirst()} and {@code isLast()} will return {@code true}.</p>
     *
     * @return {@code true} if this is the last page or there's only one page,
     *         {@code false} if there are more pages to navigate to.
     *
     * @author Philipp Borkovic
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Returns a concise string representation of the PageResponse metadata.
     *
     * <p>Provides a human-readable summary of the pagination state, including
     * page number, size, total elements, and total pages. The content list is
     * <strong>not</strong> included to keep the output concise, especially for
     * large datasets.</p>
     *
     * <p><strong>Format:</strong>
     * {@code PageResponse{page=N, size=N, totalElements=N, totalPages=N}}</p>
     *
     * @return A string representation of this PageResponse's metadata (not including content).
     *         Never null.
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("PageResponse{page=%d, size=%d, totalElements=%d, totalPages=%d}",
            page, size, totalElements, totalPages);
    }
}
