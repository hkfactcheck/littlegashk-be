package io.littlegashk.webapp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class CommonUtils {

  public static <T> List<T> readAllPages(Function<Pageable, Page<T>> pageSupplier) {
    Page<T> page = pageSupplier.apply(PageRequest.of(0, 10));
    List<T> all = new ArrayList<>(page.getContent());
    while (page.hasNext()) {
      page = pageSupplier.apply(page.nextPageable());
      all.addAll(page.getContent());
    }
    return all;
  }
}
