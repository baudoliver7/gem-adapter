/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem.http;

import com.artipie.asto.Storage;
import com.artipie.gem.GemApiKeyAuth;
import com.artipie.http.Slice;
import com.artipie.http.auth.Action;
import com.artipie.http.auth.AuthSlice;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.Permissions;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import com.artipie.http.slice.SliceDownload;
import com.artipie.http.slice.SliceSimple;
import java.util.Optional;

/**
 * A slice, which servers gem packages.
 *
 *  Ruby HTTP layer.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ParameterNumberCheck (500 lines)
 * @since 0.1
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
public final class GemSlice extends Slice.Wrap {

    /**
     * Ctor.
     *
     * @param storage The storage.
     */
    public GemSlice(final Storage storage) {
        this(storage,
            Permissions.FREE,
            (login, pwd) -> Optional.of(new Authentication.User("anonymous"))
        );
    }

    /**
     * Ctor.
     *
     * @param storage The storage.
     * @param permissions The permissions.
     * @param auth The auth.
     */
    public GemSlice(final Storage storage,
        final Permissions permissions,
        final Authentication auth) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new RtRule.All(
                        ByMethodsRule.Standard.POST,
                        new RtRule.ByPath("/api/v1/gems")
                    ),
                    new AuthSlice(
                        new SubmitGemSlice(storage),
                        new GemApiKeyAuth(auth),
                        new Permission.ByName(permissions, Action.Standard.WRITE)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        ByMethodsRule.Standard.GET,
                        new RtRule.ByPath("/api/v1/dependencies")
                    ),
                    new DepsGemSlice(storage)
                ),
                new RtRulePath(
                    new RtRule.All(
                        ByMethodsRule.Standard.GET,
                        new RtRule.ByPath("/api/v1/api_key")
                    ),
                    new ApiKeySlice(auth)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new ByMethodsRule(RqMethod.GET),
                        new RtRule.ByPath(ApiGetSlice.PATH_PATTERN)
                    ),
                    new ApiGetSlice(storage)
                ),
                new RtRulePath(
                    new ByMethodsRule(RqMethod.GET),
                    new AuthSlice(
                        new SliceDownload(storage),
                        new GemApiKeyAuth(auth),
                        new Permission.ByName(permissions, Action.Standard.READ)
                    )
                ),
                new RtRulePath(
                    RtRule.FALLBACK,
                    new SliceSimple(new RsWithStatus(RsStatus.NOT_FOUND))
                )
            )
        );
    }
}
