package kitra.awachat.next.dto;

public record ApiResponse<T>(int code, T data, String msg) {
}
