package ru.igla.tfprofiler

abstract class UseCase<Q : UseCase.RequestValues, P : UseCase.ResponseValue> {

    var requestValues: Q? = null

    abstract fun executeUseCase(requestValues: Q): Resource<P>

    /**
     * Data passed to a request.
     */
    interface RequestValues

    /**
     * Data received from a request.
     */
    interface ResponseValue


    data class Resource<out T>(
        val status: Status,
        val data: T?,
        val message: String?,
        val t: Throwable?
    ) {
        companion object {
            fun <T> success(data: T?): Resource<T> {
                return Resource(Status.SUCCESS, data, null, null)
            }

            fun <T> error(message: String? = null, t: Throwable? = null): Resource<T> {
                return Resource(Status.ERROR, null, message, t)
            }
        }

        fun isSuccess(): Boolean {
            return status == Status.SUCCESS
        }
    }

    enum class Status {
        SUCCESS,
        ERROR;
    }
}